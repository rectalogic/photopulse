/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Photica Photopulse.
 *
 * The Initial Developer of the Original Code is
 * Photica Inc.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.photopulse.imageio.cache;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import com.photica.photopulse.PhotoPulse;
import com.photica.photopulse.imageio.DecodedImage;
import com.photica.photopulse.imageio.IIOReadListener;
import com.photica.photopulse.imageio.ImageCoder;
import com.photica.photopulse.imageio.ImageTranscoder;
import com.photica.photopulse.imageio.ThumbnailDecoder;
import com.photica.photopulse.imageio.TranscodeOp;
import com.photica.photopulse.model.Photo;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.prefs.Preferences;


/**
 *  A File system based thumbnail cache. Files are mapped
 * to a path on the file system under the cache root. The file name is a hash code based
 * on the path to the file and the size of the thumbnail. The following MetaData
 * is appended to each thumbnail.

 * srcLastModified   ->long
 * thumbLastAccessed ->long
 * MDVersion         ->byte
 **/

public class ThumbnailCache implements ThumbnailDecoder, ImageTranscoder {

    public static final int THUMBNAIL_WIDTH = 150;
    public static final int THUMBNAIL_HEIGHT = 100;

    private static ThumbnailCache instance = new ThumbnailCache();
    private static ClockDaemon clockDaemon = null;

    // TODO this should be a property that can be set externally
    private static final long defaultCleanIntervalMillis = 1000L * 60L * 10L; // 10 minutes
    private static final long minCleanIntervalMillis = 1000L * 60L * 5L; // 5 minutes
    private static boolean cleanOnStartUp = false;

    private File _root = null;
    // max byte size of disk and ram cache
    private long maxBytes = defaultMaxDiskCacheBytes;

    // clean interval in millis
    private long cleanIntervalMillis = defaultCleanIntervalMillis;

    private SoftCache<File,DecodedImage> memoryCache = new SoftCache<File,DecodedImage>();

    private static final boolean DEBUG = false;
    private static final String CACHE_FILE_PREFIX="ppulse";
    private static final String CACHE_FILE_SUFFIX=".jpg";

    // Keep this in sync with uninstaller
    public static final String DEFAULT_DIR_NAME = "PhotoPulseCache";
    private static final String USER_HOME_PROPERTY = "user.home";

    // TODO should be a property that can be set extenally
    /**
     * Default value for number of bytes to store on disk
     */
    public  static final long defaultMaxDiskCacheBytes = 50000000L; // 50 Meg

    private static final String PREF_CACHE_MAX_DISK_BYTES = "CacheMaxDiskBytes";
    private static final String PREF_CACHE_HOME = "CacheHome";
    private static final String PREF_CACHE_CLEAN_INTERVAL = "CacheCleanInterval";


    public synchronized static ThumbnailCache getInstance() {
        // when someone requests and instance we set up the cleaner if the
        // interval is greater than the min interval and the clockDeamon does
        // not exist
        long ci = instance.cleanIntervalMillis;
        if( clockDaemon==null && ci >= minCleanIntervalMillis ) {
            clockDaemon = new ClockDaemon();
            clockDaemon.executePeriodically(instance.cleanIntervalMillis, instance.getCleaner(),cleanOnStartUp);
        }
        return instance;
    }


    /**
     * Create a cache using the user.home system property.
     * @see System#getProperties()
     */
    private ThumbnailCache() {
        Preferences prefs = Preferences.userNodeForPackage(getClass());

        String userHome = prefs.get(PREF_CACHE_HOME,null);
        if( userHome == null )
            userHome = System.getProperty(USER_HOME_PROPERTY) + File.separator + DEFAULT_DIR_NAME;
        _root = new File(userHome);

        maxBytes = prefs.getLong(PREF_CACHE_MAX_DISK_BYTES, defaultMaxDiskCacheBytes);
        cleanIntervalMillis = prefs.getLong(PREF_CACHE_CLEAN_INTERVAL,defaultCleanIntervalMillis );
    }

    public File getCacheRoot() {
        return _root;
    }

    /**
     * Flushes all resources being used for cache optimization. This method basically
     * clears in memory image caches. The in memory cache will be built up again. Call this
     * method before doing a memory intensive operation to ensure the VM does not run out
     * of memory.
     */
    public void flush() {
        memoryCache.clear();
    }


    /**
     * Determine if the specified file is in the cache. Note that this does not gaurantee
     * that the file will continue to be in the cache. It is possible that this method will
     * return true if the file is removed just after the check is done. This method will also
     * return true if the cached file is stale since it does not load the files to check
     * modified times.
     * @param srcPath
     * @return true if the specified file in is the cache
     */
    public boolean hasCache(File srcPath, double scaleFactor) {
        File cachePath = resolveDBPath(srcPath, scaleFactor);
        if( DEBUG ) System.out.println("CACHE CK  = " + srcPath.getName() );
        if( isInMemoryCache(cachePath)) return true;
        return cachePath.exists();
        // return cacheFileCurrent(srcPath, cachePath);

    }

    private boolean cacheFileCurrent(File srcFile, File cacheFile)  {
        RandomAccessFile raf = null;
        boolean isCurrent = false;
        try {
            if( cacheFile.exists() ) {
                    raf = new RandomAccessFile(cacheFile, "r");
                    long  sz = raf.length();
                    if( DEBUG ) System.out.println("CACHPATH SIZE = " + sz );
                    if( sz > 0 ) {
                        MetaData md = new MetaData(0,0);
                        try {
                            md = md.loadMetaData(raf);
                            if( DEBUG )System.out.println(md);
                            long srcLastModified = srcFile.lastModified();
                            if( srcLastModified == md.getSrcLastModified() ) isCurrent = true;
                            else cacheFile.delete(); // not current - delete
                        } catch (MetaDataVersionException ex ) {
                            // the cachefile exists but is not compatible so delete it
                            if( DEBUG )System.out.println("DELETE CACHE FOR: "  + srcFile.getName() + "(" + cacheFile.getName() + ")" );
                            cacheFile.delete();
                        }

                    }
                    raf.close();
                    raf = null;
                }
        } catch (IOException ex ) {
            isCurrent = false;
        } finally {
                if (raf != null ) try {
                    raf.close();
                } catch(IOException ex ) {
                    ; // can't do anything more....
                }
            }
        return isCurrent;
    }


    /**
     * Implementation of decodeThumbnail. This implementation returns a cached
     * thumbnail and if necessary, adds it to the cache.
     * @see com.photica.photopulse.imageio.ThumbnailDecoder
     */
    public DecodedImage decodeThumbnail(Photo photo, int scaleWidth, int scaleHeight, IIOReadListener listener, boolean fastDecode) throws IOException {
        double scaleFactor = ImageCoder.getInstance().computeScaleFactor(photo.getWidth(), photo.getHeight(),
                scaleWidth, scaleHeight);
        // May consider using fastGet() here if this is too slow
        if (fastDecode)
            return get(photo.getFile(), scaleFactor, listener);
        else
            return add(photo.getFile(), scaleFactor, listener);
    }

    /**
     * Transcode an image with caching.
     * Does not cache the image if it is being cropped.
     * scaleType may be ignored if cached image is used.
     * This implementation is for quick and dirty previews.
     */
    public DecodedImage transcodeImage(File sourceImageFile, String targetFormat,
            Rectangle cropRect, double scaleFactor, int scaleType, TranscodeOp op, Object output) throws IOException {

        DecodedImage dimage = null;
        // Retrieve from cache if not cropping.
        if (cropRect == null)
            dimage = get(sourceImageFile, scaleFactor, null);

        // If image was not in cache, or we are cropping, then decode from source.
        // Do not cache.
        if (cropRect != null || dimage == null) {
            dimage = ImageCoder.getInstance().decodeImage(sourceImageFile, cropRect, scaleFactor, 0, 0, scaleType, null);
        }

        if (op != null) {
            BufferedImage filteredImage = op.filter(dimage.getImage());
            dimage = new DecodedImage(dimage.getSourceFile(), dimage.getSourceFormat(), filteredImage);
        }

        ImageCoder.getInstance().encodeImage(dimage, targetFormat, output);
        return dimage;
    }


    /**
     * Get an image from the ram cache (very fast) or return null.
     */
    public DecodedImage fastGet(File srcPath, double scaleFactor)  {
        if( DEBUG ) System.out.println("CACHE FAST GET = " + srcPath.getName() );
        File cachePath = resolveDBPath(srcPath, scaleFactor);

        return  getFromMemoryCache(cachePath);
    }

    /**
     *
     * Get a thumbnail from the cache or throw exception if it does
     * not exist. Use this method if you want fast access to a thumbnail image.
     * The assumption is that thumbnails will load quickly and it it acceptable
     * to load them on a UI thread. It there is no cached image then this method
     * will throw an exception. If there is no need for a fast load or if you want to
     * and image back even in it must be created from the original then use add(..)
     *
     * @param srcPath the file system path to the source image
     * @return image
     *
     **/
    private DecodedImage get(File srcPath, double scaleFactor, IIOReadListener listener) throws IOException {
        if( DEBUG ) System.out.println("CACHE GET = " + srcPath.getName() );
        return get(srcPath, resolveDBPath(srcPath, scaleFactor), listener);
    }

    private DecodedImage get(File srcPath, File cachePath, IIOReadListener listener) throws IOException {
        RandomAccessFile raf = null;
        DecodedImage img = null;
        try {
            // check MRU - return if exists
            img = getFromMemoryCache(cachePath);
            if (img != null) {
                if( DEBUG ) System.out.println("USE MRU VERSION FOR " + srcPath.getName());
                return img;
            }
            // check on disk cache ensuring file stil matches disk file
            if( cachePath.exists() ) {
                boolean mdVersionOK = true;
                if( DEBUG ) System.out.println("USE DISK VERSION FOR " + srcPath.getName());
                raf = new RandomAccessFile(cachePath, "rw");
                MetaData md = new MetaData( 0L, 0L);
                try {
                    md = md.loadMetaData(raf);
                } catch (MetaDataVersionException ex ) {
                    mdVersionOK = false;
                }
                // Get the image if it's current/correct.
                // We don't delete invalid images since some combination of clean
                // and add should take care of this.
                if (mdVersionOK && md.getSrcLastModified() == srcPath.lastModified()) {
                    md.setThumbLastAccessed(raf);
                    raf.close();
                    raf=null; // no need to close in finally if all goes well
                    img = ImageCoder.getInstance().decodeImage(cachePath,null,1.0,0,0,ImageTranscoder.SCALE_BILINEAR,listener);
                    putInMemoryCache(cachePath, img);
                }
            }
        } finally {
            try {
                if( raf != null ) raf.close();
            } catch (IOException ex ) {
                PhotoPulse.logException(ex);
            }
        }
        return img;
    }


    /**
     * add the specified file to the cache
     */
    public DecodedImage add(File srcPath, double scaleFactor, IIOReadListener listener) throws IOException {
        if( DEBUG ) System.out.println("CACHE ADD  = " + srcPath.getName() );
        DecodedImage img = null;
        File cachePath = resolveDBPath(srcPath, scaleFactor);
        // call get first - to make sure we don't need to do extra work
        // get will throw an exception if the image exists but is invalid so we overwrite
        // existing images if it fails
        try {
            img = get(srcPath, cachePath, listener);
        } catch (IOException e) {
            // If this fails, fall through and decode from source
        }

        img = ImageCoder.getInstance().decodeImage(srcPath,null,scaleFactor,0,0,ImageTranscoder.SCALE_SUBSAMPLE,listener);
        // put in MRUCache first - if we have disk IO problems we still get some
        // caching
        putInMemoryCache(cachePath, img);
        // we have a thumb that can be used - catch exceptions and report errors
        // but retun the valid thumb
        writeThumb(cachePath, img, srcPath.lastModified());

        return img;
    }


    /**
     * Write the thumbnail image setting lastException if there was an error.
     * @param cachePath
     * @param img
     */
    private void writeThumb(File cachePath, DecodedImage img, long srcLastModified) {
        // Override close() as a noop so image encoding does not close us.
        class UncloseableRandomAccessFile extends RandomAccessFile {
            public UncloseableRandomAccessFile(File file, String mode) throws FileNotFoundException {
                super(file, mode);
            }
            public void close() throws IOException {
                // Do nothing
            }
            public void reallyClose() throws IOException {
                super.close();
            }
        };

        UncloseableRandomAccessFile thumbRAF = null;
        File tmpFile = null;
        try {
            if( DEBUG ) System.out.println("MRU PUT " + cachePath.getName());
            File parent = cachePath.getParentFile();
            if( parent != null ) parent.mkdirs();
            // use tmpFile to (hopefully) get atomic create operation for thumb

            tmpFile = File.createTempFile(CACHE_FILE_PREFIX, CACHE_FILE_SUFFIX, parent);
            thumbRAF = new UncloseableRandomAccessFile(tmpFile, "rw");
            // Encoding will attempt to close thumbRAF, but can't.
            // So we can still append metadata and then close.
            // We want to pass a RAF instead of OutputStream here because it is much more efficient
            // (avoids ImageIO temp files)
            ImageCoder.getInstance().encodeImage(img, ImageTranscoder.FORMAT_JPEG, thumbRAF);
            long now = System.currentTimeMillis();
            MetaData md = new MetaData(srcLastModified, now );
            md.appendMetaData(thumbRAF);
            if( DEBUG ) System.out.print("RENAME...");
            thumbRAF.reallyClose();
            // delete the tmpfile if it exists - rename may fail to overwrite
            // file.
            if( cachePath.exists() ) cachePath.delete();
            tmpFile.renameTo(cachePath);
            // set to null so no close/delete is done in finally
            thumbRAF = null;

            if (DEBUG ) System.out.println("OK");
        } catch (IOException ex ) {
            if( DEBUG ) ex.printStackTrace();
            PhotoPulse.logException(ex);
        } finally {
            // clean up the thumb file if necessary
            if( thumbRAF != null ) {
                try {
                    thumbRAF.reallyClose();
                    tmpFile.delete();
                } catch (IOException ex ) {
                    PhotoPulse.logException(ex);
                }
            }
        }
    }


    private File resolveDBPath(File realPath, double scaleFactor) {
        String md5 = getFileMD5(realPath, scaleFactor);
        return new File(_root, md5 + ".jpg");
    }

    private String getFileMD5(File f, double scaleFactor) {
        String s = f.getAbsolutePath() + File.separator + scaleFactor;

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            assert(false);
        }

        byte[] digested = null;
        try {
            digested = md.digest(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            assert(false);
        }

        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < digested.length; x++) {
            sb.append(Character.forDigit((digested[x]>>>4) & 0x0f, 16));
            sb.append(Character.forDigit(digested[x] & 0x0f, 16));
        }
        return sb.toString();
    }


    /**
     * Calculate the size of the cache directory and delete oldest files until size is
     * less than max size. This is an expensive (lots of IO - may take a long time)
     * operation. This method is synchronized to prevent it from running concurrently in two
     * threads. It may still execute concurrently in 2 vms or if two different instances use the same cache
     *  - resulting in a larger number
     * of files being cleaned out.
     * @param headRoom (leave  headRoom bytes of space when cleaning up)
     */
    public synchronized void clean(long headRoom) {
        File [] files = _root.listFiles();
        long maxSize = maxBytes - headRoom;
        long sz = 0L;
        for( int x=0;x<files.length;x++) {
            sz += files[x].length();
        }
        if (sz > maxSize ) {
            Comparator<File> comp = new Comparator<File>() {
                public final int compare(File f1, File f2) {
                    long l1 = f1.lastModified();
                    long l2 = f2.lastModified();
                    if(l1==l2) return 0;
                    if( l1<l2) return -1;
                    return 1;
                }
                public final boolean equals(Object obj) {
                    return obj == this;
                }
            };
            Arrays.sort(files, comp);
            int idx = 0;
            while( sz > maxBytes && idx < files.length ) {
                File f = files[idx++];
                if( DEBUG) System.out.println("DELETE " + f.getName());
                if( !isInMemoryCache(f)) {
                    sz -= f.length();
                    f.delete();
                }
            }
        }

    }

    private Runnable getCleaner() {
        return new Runnable() {
            public void run() {
                clean(1000000L);
            }
        };
    }

    private boolean isInMemoryCache(File file) {
        return memoryCache.containsKey(file);
    }

    private void putInMemoryCache(File key, DecodedImage image) {
        memoryCache.put(key, image);
    }

    private DecodedImage getFromMemoryCache(File key) {
        return memoryCache.get(key);
    }
}
