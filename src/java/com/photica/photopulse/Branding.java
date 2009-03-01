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
 * Portions created by the Initial Developer are Copyright (C) 2009
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
 package com.photica.photopulse;

/**
 * This class is filtered as part of the build process.
 * It localizes any branding specific changes in a build.
 */
public interface Branding {
    // Require a valid license, do not allow trial installation
    public static final boolean REQUIRE_LICENSE = ${branding.requireLicense};
    // Root of license keys (8 bits)
    public static final int LICENSE_VERSION = ${branding.licenseVersion};
    // Enable expert mode
    public static final boolean ENABLE_EXPERTMODE = ${branding.enableExpertMode};
    // True if expert mode should be on by default
    public static final boolean DEFAULT_EXPERTMODE = ${branding.defaultExpertMode};

    // Comma separated list of Effect key names to be exclusively included by default, or null
    public static final String DEFAULT_BEGINTRANS_INCLUDES = ${branding.begintrans.includes};
    // Comma separated list of BeginTrans key names to be exclusively included by default, or null
    public static final String DEFAULT_EFFECT_INCLUDES = ${branding.effect.includes};
    // Comma separated list of EndTrans key names to be exclusively included by default, or null
    public static final String DEFAULT_ENDTRANS_INCLUDES = ${branding.endtrans.includes};

    // Enable project file save/load
    public static final boolean ENABLE_PROJECTS = ${branding.enableProjects};
    // URL to purchase license
    public static final String PURCHASE_URL = "${branding.purchase.url}";
    // URL to product site
    public static final String PRODUCT_URL = "${branding.product.url}";
    // URL to register product
    public static final String REGISTER_URL = "${branding.register.url}";
    // Product ID name
    public static final String PRODUCT_ID = "${branding.productID}";
    // Product name (for UI)
    public static final String PRODUCT_NAME = "${branding.productName}";
}
