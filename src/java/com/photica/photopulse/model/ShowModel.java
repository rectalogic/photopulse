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
 
package com.photica.photopulse.model;

import com.photica.photopulse.skin.Skin;
import com.photica.photopulse.skin.SkinManager;

import javax.print.attribute.EnumSyntax;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

// TODO document selectionModel setLeadAnchorNotificationEnabled settings
//  also need to add to modelIO...


/**
 * An object that describes a PhotoPulse show. This object can be passed
 * around to various PhotoPulse modules and operated on. In addition, this
 * obbject should be able to be serialized to and from XML. The model
 * is not threadsafe.
 */
public class ShowModel implements Cloneable {

    //-------------------- Constants -----------------------------------------------
    // how is the mp3 track stored  in the show
    public static enum MP3Mode {
        EXTERNAL("External"),
        INTERNAL("Internal"),
        NONE("None");

        private String oldName;

        MP3Mode(String oldName) {
            this.oldName = oldName;
        }

        // Must use toString() and not name() when generating XML
        public String toString() {
            return oldName;
        }
    };

    // What to do at the end of a show
    // LOOP the show
    // PAUSE on the last frame displaying last photo and controller (forces last endtrans to None)
    // STOP on the last frame - displaying background
    // CLOSE - quit if exe and send a window.close command if html
    public static enum EndShowMode {
        LOOP("Loop"),
        PAUSE("Pause"),
        STOP("Stop"),
        CLOSE("Close");

        private String oldName;

        EndShowMode(String oldName) {
            this.oldName = oldName;
        }

        // Must use toString() and not name() when generating XML
        public String toString() {
            return oldName;
        }
    };

    // Types of shows that we can generate
    public static enum ShowType {
        EXE("exe"),
        HTM("htm"),
        SWF("swf"),
        MHT("mht"),
        WMV("wmv");

        private String oldName;

        ShowType(String oldName) {
            this.oldName = oldName;
        }

        // Must use toString() and not name() when generating XML
        public String toString() {
            return oldName;
        }
    };


    // Property names
    public static final String BACKGROUND_COLOR_PROPERTY = "backgroundColor";
    public static final String DEFAULT_EFFECT_DURATION_PROPERTY = "defaultEffectDuration";
    public static final String DEFAULT_TRANSITION_DURATION_PROPERTY = "defaultTransitionDuration";
    public static final String FRAMERATE_PROPERTY = "frameRate";

    public static final String MP3_FILE_PROPERTY = "mp3File";

    public static final String MP3_MODE_PROPERY= "mp3Mode";
    public static final String SHOW_TYPE_PROPERTY = "showType";
    public static final String SKIN_PROPERTY = "skin";
    public static final String ENDSHOW_MODE_PROPERTY = "endShowMode";

    public static final String EXPORT_DIRECTORY_PROPERTY = "exportDirectory";
    public static final String PHOTO_DIRECTORY_PROPERTY = "photoDirectory";
    public static final String MP3_DIRECTORY_PROPERTY = "mp3Directory";
    public static final String EXPORT_FILE_PROPERTY = "exportFile";

    public ShowModel() {
        this(new ShowSelectionModel(), new ShowSelectionModel(), new ShowSelectionModel());
    }

    /**
     * Copy forward directories, skin and trans/effect selections from a previous model.
     */
    public ShowModel(ShowModel oldModel) {
        this((ShowSelectionModel)oldModel.getBeginTransitionSelectionModel().clone(),
                (ShowSelectionModel)oldModel.getEffectSelectionModel().clone(),
                (ShowSelectionModel)oldModel.getEndTransitionSelectionModel().clone());
        this.mp3Directory = oldModel.getMP3Directory();
        this.exportDirectory = oldModel.getExportDirectory();
        this.photoDirectory = oldModel.getPhotoDirectory();
        this.skin = oldModel.getSkin();
    }

    public ShowModel(BitSet beginTransitionSelection, BitSet effectSelection, BitSet endTransitionSelection) {
        this();
        if (beginTransitionSelection != null)
            getBeginTransitionSelectionModel().setSelection(beginTransitionSelection);
        if (effectSelection != null)
            getEffectSelectionModel().setSelection(effectSelection);
        if (endTransitionSelection != null)
            getEndTransitionSelectionModel().setSelection(endTransitionSelection);
    }

    private ShowModel(ShowSelectionModel beginTransitionSelection, ShowSelectionModel effectSelection, ShowSelectionModel endTransitionSelection) {
        this.beginTransitionSelectionModel = beginTransitionSelection;
        this.effectSelectionModel = effectSelection;
        this.endTransitionSelectionModel = endTransitionSelection;
    }

    public void setTrackModifications(boolean trackModifications) {
        if (trackModifications && modificationListener == null) {
            // Listen on our lists so we can track modifications
            modificationListener = new ModifiedListener();
            getBeginTransitionSelectionModel().addShowSelectionListener(modificationListener);
            getEffectSelectionModel().addShowSelectionListener(modificationListener);
            getEndTransitionSelectionModel().addShowSelectionListener(modificationListener);
            getPhotoEffectList().addListDataListener(modificationListener);
        }
        else if (!trackModifications && modificationListener != null) {
            getBeginTransitionSelectionModel().removeSelectionListener(modificationListener);
            getEffectSelectionModel().removeSelectionListener(modificationListener);
            getEndTransitionSelectionModel().removeSelectionListener(modificationListener);
            getPhotoEffectList().removeListDataListener(modificationListener);
            modificationListener = null;
        }
    }

    public boolean isTrackModifications() {
        return modificationListener != null;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if( _propertyChangeSupport==null)
            _propertyChangeSupport = new PropertyChangeSupport(this);
        _propertyChangeSupport.addPropertyChangeListener(listener);
    }
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if( _propertyChangeSupport==null)
            _propertyChangeSupport = new PropertyChangeSupport(this);
        _propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if( _propertyChangeSupport != null ) {
            _propertyChangeSupport.removePropertyChangeListener(listener);
        }
    }
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if( _propertyChangeSupport != null) {
            _propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
        }
    }

    /**
     * Get the show type. For example    SHOW_TYPE_EXE or    SHOW_TYPE_HTML.
     * This should always return a valid show type.
     */
    public ShowType getShowType() {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : getShowType");
        return _showType;
    }

    /**
     *
     * @param newType the new show type
     */
    public void setShowType(ShowType newType) {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : setShowType");
        if (propertiesEqual(_showType, newType))
            return;
        ShowType oldType = _showType;
        _showType = newType;
        firePropertyChange(
                SHOW_TYPE_PROPERTY,
                oldType,
                _showType
        );

    }



    // background color
    /**
     *
     * @return the current show Background Color
     */
    public Color getBackgroundColor() {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : getBGColor");
        return backgroundColor;
    }

    /**
     *
     * @param color - the new background color for the show. Null values are ignored.
     */
    public void setBackgroundColor(Color color) {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : setBGColor");
        if (propertiesEqual(backgroundColor, color))
            return;
        // if setting the existing value then return
        if ( color == null) {
            return;
        }
        Color oldVal = backgroundColor;
        backgroundColor = color;
        firePropertyChange(BACKGROUND_COLOR_PROPERTY, oldVal, backgroundColor);
    }

    //  The Theme for this show
    /**
     * Set the Skin/Theme for this show. There is no checking for theme
     * values. It is assumed that the name and dimension can be properly
     * dealy with by the theme manager. If there is an error in finding a theme
     * the applicaiton should get the user to select a valid theme. One case where
     * this can happed is when a user deleted a custom theme.
     * @param skin null values are ignored.
     */
    public void setSkin(Skin skin) {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : setSkin");
        if (propertiesEqual(this.skin, skin))
            return;
        Skin oldSkin = this.skin;
        if ( skin == null)
            return;
        this.skin = skin;
        firePropertyChange(
                SKIN_PROPERTY,
                oldSkin,
                this.skin
        );
    }

    public Skin getSkin() {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : getSkin");
        return skin;
    }

    // mp3 selection - internal, external or none
    public MP3Mode getMP3Mode() {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : getMP3Mode");
        return mp3Mode;
    }

    public void setMP3Mode(MP3Mode mode) {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : setMP3Mode: " + mode);
        if (propertiesEqual(mp3Mode, mode))
            return;
        MP3Mode oldPref = mp3Mode;
        mp3Mode = mode;
        firePropertyChange(
            MP3_MODE_PROPERY,
                oldPref,
                mp3Mode
        );


    }



    public MP3 getMP3() {
        return mp3;
    }
    public void setMP3(MP3 file ) {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : setMP3: " + file);
        if (propertiesEqual(mp3, file))
            return;

        MP3 _old = mp3;
        mp3 = file;

        firePropertyChange(
                MP3_FILE_PROPERTY,
                _old,
                mp3
        );
    }

    public EndShowMode getEndShowMode() {
        if (_DEBUG) System.out.println("PHOTOPULSE SHOW : getEndShowMode");
        return endShowMode;
    }

    public void setEndShowMode(EndShowMode mode) {
        if (_DEBUG) System.out.println("PHOTOPULSE SHOW : setEndShowMode: " + mode);
        if (propertiesEqual(endShowMode, mode))
            return;
        EndShowMode oldPref = endShowMode;
        endShowMode = mode;
        firePropertyChange(ENDSHOW_MODE_PROPERTY, oldPref, endShowMode);
    }

    ///////////////// BEGIN TRANS /////////////////////////////////////////




    /**
     *
     * @return a copy of the current selection.
     */
    public ShowSelectionModel  getBeginTransitionSelectionModel() {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : getBeginTransitionSelectionModel: ");
        return beginTransitionSelectionModel;
    }


    public ShowSelectionModel getEndTransitionSelectionModel() {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : getEndTransitionSelectionModel: ");
        return endTransitionSelectionModel;
    }


    public ShowSelectionModel  getEffectSelectionModel() {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : getEffectSelectionModel: ");
        return effectSelectionModel;
    }



    // TODO implement these as a filtered list to improve performance
    // subclass Ab
    public static <T> List<T> applyListSelection(ShowSelectionModel targetModel, List<T> targetList) {
        int sz = targetList.size();
        ArrayList<T> ret = new ArrayList<T>(sz);
        for (int x = 0; x < sz; x++) {
            if (targetModel.isSelectedIndex(x)) {
                ret.add(targetList.get(x));
            }
        }
        return ret;
    }


    /**
     *     Get the global transition duration default for this show
     * @return
     */
    public double getDefaultTransitionDuration() {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : getDefaultTransitionDuration:" + defaultTransitionDuration);
        return defaultTransitionDuration;
    }

    public void setDefaultTransitionDuration(double newVal) {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : setDefaultTransitionDuration");
        if (defaultTransitionDuration == newVal)
            return;
        if (newVal < 0) {
            String errMsg = "Input Transition Value Must Be Greater Than 0 : " + newVal;
            throw new IllegalArgumentException(errMsg);
        }
        double oldVal = defaultTransitionDuration;
        defaultTransitionDuration = newVal;
        firePropertyChange(
                DEFAULT_TRANSITION_DURATION_PROPERTY,
                new Double(oldVal),
                new Double (defaultTransitionDuration)
        );
    }

    public double getDefaultEffectDuration() {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : getDefaultEffectDuration:" + defaultEffectDuration);
        return defaultEffectDuration;
    }

    public void setDefaultEffectDuration(double d) {

        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : setDefaultEffectDuration");
        if (defaultEffectDuration == d)
            return;
        if (d < 0.0) {
            // TODO - error message shoud go in resource bundle
            String errMessage = "Effect Duration Must be >= 0: " + d;
            throw new IllegalArgumentException(errMessage);
        }
        double oldVal = defaultEffectDuration;
        defaultEffectDuration = d;
        firePropertyChange(DEFAULT_EFFECT_DURATION_PROPERTY, new Double(oldVal), new Double(defaultEffectDuration));
    }

    public float getFrameRate() {
        if (_DEBUG) System.out.println("PHOTOPULSE SHOW : getFrameRate:" + frameRate);
        return frameRate;
    }

    public void setFrameRate(float frameRate) {
        if (_DEBUG) System.out.println("PHOTOPULSE SHOW : setFrameRate");
        if (this.frameRate == frameRate)
            return;
        if (frameRate <= 0.0) {
            // TODO - error message shoud go in resource bundle
            throw new IllegalArgumentException("FrameRate Must be > 0: " + frameRate);
        }
        double oldFrameRate = this.frameRate;
        this.frameRate = frameRate;
        firePropertyChange(FRAMERATE_PROPERTY, new Float(oldFrameRate), new Float(this.frameRate));
    }

    /**
     * The ListModel for PhotoPulse Images
     * This object contains a list of immutable Image objects (PhotoEffects)that define
     * the state of the images in the show. In order to change a property you must
     * replace an object in the list
     */
    public ShowList getPhotoEffectList() {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : getImageListModel");
        return imageListModel;
    }



    public double computeShowDuration() {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : computeShowDuration: ");
        double myDuration = 0.0;
        double tmpDuration = 0.0;
        // Object [] items = _myListModel.toArray();
        // duration is sum of the effect times and transition times
        // however the transitions overlap so this must be dealy with
        // TODO XXX  DefaultListModel images = _myPhotoPulseShow.getImageListModel();
        ShowList images = getPhotoEffectList();
        int sz = images.getSize();
        for( int x=0;x<sz;x++) {
            PhotoEffects f = (PhotoEffects)images.getElementAt(x);
            if( x==0 ) {
                    myDuration += f.getBeginTransitionDuration();
            } else {
                // get longest of last end diration of this begin duration
                myDuration += Math.max(tmpDuration, f.getBeginTransitionDuration());
            }
            myDuration += f.getEffectDuration();
            tmpDuration = f.getEndTransitionDuration();
        }
        // add the final end duration
        myDuration += tmpDuration;
        return myDuration;
    }

    /**
     *
     * @return a showModel instance with selections clear and a new copy of
     * the imagelist and no property listeners. The clone will not track modifications.
     */
    @Override public ShowModel clone() {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : clone: ");
        ShowModel myClone = null;
        try {
            // get a shallow copy of all variables
            myClone = (ShowModel)super.clone();
            // get selection model clones without listeners
            myClone.beginTransitionSelectionModel = (ShowSelectionModel)beginTransitionSelectionModel.clone();
            myClone.endTransitionSelectionModel = (ShowSelectionModel)endTransitionSelectionModel.clone();
            myClone.effectSelectionModel = (ShowSelectionModel)effectSelectionModel.clone();
            // make a new imageList with the same items as the current model - no listeners
            myClone.imageListModel = new ShowList(imageListModel);
            // create a new propertChangeSupport instance for this clone.
            myClone._propertyChangeSupport = new PropertyChangeSupport(myClone);
        } catch (CloneNotSupportedException ex ) {
            throw new IllegalStateException("Clone Operation Failed");
        }
        return myClone;
    }

    /**
     * The PhotoDirectory associated with this show. This may or not be the same
     * as the directory last used to load an image. This is up to the application.
     * @return last photo directory used or null
     */
    public File getPhotoDirectory() {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : getPhotoDirectory: ");
        return photoDirectory;
    }
    /**
     * Get the default MP3 directory for this show. This is not necessarly the
     * same as the last directory used for setting the mp3 file. Setting this is
     * up to the application.
     * @return the default mp3 directory for this show
     */
    public File getMP3Directory() {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : getMP3Directory: ");
        return mp3Directory;
    }


    public File getExportDirectory() {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : getExportDirectory: ");
        return exportDirectory;
    }

    public void setExportDirectory(File exportDir) {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : setExportDirectory: " + exportDir);
        if (propertiesEqual(this.exportDirectory, exportDir))
            return;
        File oldVal = this.exportDirectory;
        this.exportDirectory = exportDir;
        firePropertyChange(
                    EXPORT_DIRECTORY_PROPERTY,
                    oldVal,
                    this.exportDirectory
            );
    }

    public void setMP3Directory(File mp3Dir) {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : setMP3Directory: " + mp3Dir);
        if (propertiesEqual(this.mp3Directory, mp3Dir))
            return;
        File oldVal = this.exportDirectory;
        this.mp3Directory = mp3Dir;
        firePropertyChange(
                    MP3_DIRECTORY_PROPERTY,
                    oldVal,
                    this.mp3Directory
            );
    }
    public void setPhotoDirectory(File photoDirectory) {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : setPhotoDirectory: " + photoDirectory);
        if (propertiesEqual(this.photoDirectory, photoDirectory))
            return;
        File oldVal = this.photoDirectory;
        this.photoDirectory = photoDirectory;
        firePropertyChange(
                    PHOTO_DIRECTORY_PROPERTY,
                    oldVal,
                    this.photoDirectory
            );
    }



    public File getExportFile() {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : getExportFile: " );
        return exportFile;
    }

    public void setExportFile(File exportFile) {
        if( _DEBUG) System.out.println("PHOTOPULSE SHOW : setExportFile: " + exportFile );
        if (propertiesEqual(this.exportFile, exportFile))
            return;
        File oldVal = this.exportFile;
        this.exportFile = exportFile;
        firePropertyChange(
                    EXPORT_FILE_PROPERTY,
                    oldVal,
                    this.exportFile
            );

    }

    /**
     * Returns true if model has been modified.
     */
    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean modified) {
        isModified = modified;
    }

    // XXX This is calculated - we could cache this value and update
    // XXX when the photoeffects list is modified
    /**
     *
     * @param index
     * @return the start time of the begintrans of the selected image
     */
    public static double computeStartTime(ShowList l, int index) {
        int sz = l.size();
        if( index >= sz ) return -1; // invalid index
        // do all the photos before the current index.
        sz = index;
        double dur = 0.0;
        PhotoEffects pe = null;
        for( int x=0;x<sz;x++ ) {
            pe = (PhotoEffects)l.get(x);
            // get the begin tran duration
            if( x==0) {
                dur += pe.getBeginTransitionDuration();
            }
            else {
                PhotoEffects prev = (PhotoEffects)l.get(x-1);
                dur += Math.max(
                        prev.getEndTransitionDuration(),
                        pe.getBeginTransitionDuration()
                );
            }
            // the effect duration
            dur += pe.getEffectDuration();
        }
        // for the last photo we add the end trans - but we need
        // to take into account the case where the BT is shorter than
        // the previous ET
        double etDur = 0.0;
        if( pe != null ) etDur = pe.getEndTransitionDuration();
        pe = (PhotoEffects)l.get(index);
        double btDur = pe.getBeginTransitionDuration();
        double delta = etDur - btDur;
        if ( delta > 0 ) {
            dur += delta;
        }
        return dur;
    }


    private void firePropertyChange(String propertyName, Object oldValue, Object newValue)  {
        // Change modified state if we are tracking
        if (modificationListener != null)
            setModified(true);
        if(_propertyChangeSupport != null ) {
            _propertyChangeSupport.firePropertyChange(
                    propertyName,
                    oldValue,
                    newValue);
        }
    }

    private boolean propertiesEqual(Object oldValue, Object newValue) {
        return (oldValue == null && newValue == null) || (oldValue != null && oldValue.equals(newValue));
    }

    /**
     * Listens to selections and photo list and keeps isModified up to date
     */
    private class ModifiedListener implements ListSelectionListener, ListDataListener {
        public void valueChanged(ListSelectionEvent e) {
            setModified(true);
        }
        public void intervalAdded(ListDataEvent e) {
            setModified(true);
        }
        public void intervalRemoved(ListDataEvent e) {
            setModified(true);
        }
        public void contentsChanged(ListDataEvent e) {
            setModified(true);
        }
    }

    /////////////////// Private Variables //////////////////////////////////
    // default is html show.
    private ShowType  _showType = ShowType.HTM;
    // private int  _showType = ShowModel.SHOW_TYPE_EXE;
    // default action is to quit at end
    // TODO private int _showEndAction = ShowModel.SHOW_END_QUIT;

    // the default bg color
    public static final Color DEFAULT_BACKGROUND_COLOR = Color.GRAY;
    public static final double DEFAULT_TRANSITION_DURATION = 3.0;
    public static final double DEFAULT_EFFECT_DURATION = 5.0;
    public static final float DEFAULT_FRAMERATE = 12f;
    public static final double DEFAULT_PHOTO_SCALE = 1.0;
    // the bcolor for this show
    private Color backgroundColor = DEFAULT_BACKGROUND_COLOR;
    private Skin skin = SkinManager.BUILTIN_SMALL_43;
    // the mp3 pref
    private MP3Mode  mp3Mode = MP3Mode.NONE;
    private EndShowMode endShowMode = EndShowMode.PAUSE;
    private ShowSelectionModel beginTransitionSelectionModel;
    private ShowSelectionModel endTransitionSelectionModel;
    private ShowSelectionModel effectSelectionModel;
    private double defaultTransitionDuration = DEFAULT_TRANSITION_DURATION;
    private double defaultEffectDuration = DEFAULT_EFFECT_DURATION;
    private float frameRate = DEFAULT_FRAMERATE;
    private ShowList imageListModel = new ShowList();
    // TODO store here? or in a listener? private boolean licensed = false;
    private PropertyChangeSupport _propertyChangeSupport = null;
    private MP3 mp3  = null;
    private boolean _DEBUG = false;
    private File photoDirectory = null;
    private File mp3Directory = null;
    private File exportDirectory = null;
    private File exportFile = null;

    private ModifiedListener modificationListener = null;
    private boolean isModified = false;

}
