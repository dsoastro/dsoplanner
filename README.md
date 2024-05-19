# About
DSO Planner is an astronomy observation planning tool for Android devices with excellent star charting capabilities made by active and experienced amateur observers with a passion for visual observations. It has large integrated deep sky objects databases and provides an opportunity to create any number of user own object databases. DSO Planner boasts the largest star catalog among all Android astronomy applications (USNO UCAC4, 113 mn stars). The application excels at creating observation plans on the fly, has powerful note taking capabilities, PushTo and GoTo support and night (red) mode.  

Before installation please make sure that you have at least 2 GB of free space on your internal SD card to download application databases!

* Star catalogs. USNO UCAC4 (full star coverage to 16m, 113 mn stars), Tycho-2 (2.5 mn stars), Yale bright star catalog (9 000 stars)
* Deep sky catalogs. NgcIc (12 000 objects including Messier, Caldwell and Herschel 400 objects), SAC (Saguaro Astronomy Club database, 10 000 objects), UGC (13 000 objects), Lynds dark and bright nebula (3 000 objects), Barnard dark nebula (350 objects), SH2 (300 objects), PK (1 500 planetary nebula), Abell cluster of galaxies (2 700 objects), Hickson Compact Group (100 objects), PGC (1 600 000 galaxies)
* Double Star catalog. Brightest Double Stars (2 300 stars), Washington Double Star Catalog (120 000 stars), double stars from Yale catalog. Info Panel with PA and separation for each component.
* Comet support. Orbital elements of around 700 observable comets could be automatically updated via internet
* Minor planet support. Database of 10 000 brightest minor planets
* Famous Steve Gottlieb Notes attached to NGCIC objects
* Custom catalogs. Unlimited capability to create fully searchable own catalogs
* Cross-match names database. Search objects by less common names
* DSS imagery support. Download DSS images of any sky portion into offline cache and overlay it on the star chart
* Offline images. Integrated set of images of most NgcIc objects, opportunity to add own images when creating custom catalogs
* Nebula contours. Contours of famous nebulae
* Object contours. Ellipse in real dimension and orientation
* Night mode. Fully red screen with red keyboard and menus
* PushTo for dobsonian mounts with setting circles. Level your dobsonian mount and perform one star alignment. The app will automatically recalculate az/alt numbers to easily hunt the object
* GoTo for Meade and Celestron controllers with bluetooth dongle
* Unique visibility tool. Only objects that are visible with selected equipment in the current sky conditions could be shown on the Star Chart (for objects from NGCIC/SAC/PGC catalogs)
* Planning tool. Filter any objects database by observer location, sky condition, astronomical equipment, time range of observation and object features (type, dimension, magnitude, minimal altitude, visibility and other fields of own custom catalogs). Remove duplicate objects when searching in intersecting databases. Create up to 4 observation lists. Easily track observed and remaining to be observed objects with note taking tool
* Import tool. Import observation lists in Sky Safari and Sky Tools format. Use pre-compiled Night Sky Observer Guide observation lists.
* Note taking. Take text and/or audio notes
* Observing places. GPS, manual coordinates, custom lists. Database with 24 000 cities worldwide
* Equipment. Keep track of all your telescopes and eyepieces. Use them for object visibility calculation and star charting. Use 500 popular eyepieces database
* Twilight calculator. Calculation of the full darkness for a current night and for a month ahead.
* 2 visual themes (bright and dark)
* Powerful share/export/import capabilities (of databases, observation lists, notes)

The detailed documentation is available at https://dsoplanner.com

# Application versions
There are three versions available: Pro, Plus and Basic. Pro version has the most extensive catalogs (around 2 Gb). Plus version is suitable for most needs and takes around 500 Mb (it differs from Pro version only in the number of UCAC4 stars and PGC objects), Basic version is lightweight (around 100 Mb).

**Star Chart Layers**  

| Database | Basic | Plus | Pro |
| --- | --- | --- | --- |
| USNO UCAC4 (star) | - | 15.4M to 14m 	| 113M to 16m |
| Tycho-2 (star) | 0.6M to 10.7m | 2.5M to 12m | 2.5M to 12m |
| Yale bright star catalog (star) | 9 000 | 9 000 | 9 000 | 
| Yale bright star catalog (double star) | 500 | 3 100 | 3 100 |
| NGCIC/SAC (object) | 3 100 | 14 400 | 14 400 |
| PGC (Principal Galaxies Catalog) | - | 153 000 | 1 600 000 |

**Object databases**  

| Database | Basic | Plus   | Pro |
| --- |-------|--------| --- |
| Messier | 110   | 110    | 110 |
| Caldwell | 109   | 109    | 109 |
| Herschel 400 | 400   | 400    | 400 | 
| NGCIC | 3 100 | 12 000 | 12 000 |
| SAC (Saguaro Astronomy Club database) | - | 10 000 | 10 000 |
| UGC (Uppsala General Catalogue of Galaxies) | - | 13 000 | 13 000 |
| PGC (not including NGCIC and UGC galaxies) | - | -      | 45 000 to 16M |
| Lynds dark nebula (Lynds' Catalogue of Dark Nebulae) | - | 1 800  | 1 800 |
| Lynds bright nebula (Lynds' Catalogue of Bright Nebulae) | - | 1 100  | 1 100 |
| Barnard (Barnard's Catalogue of Dark Objects) | - | 350    | 350 |
| SH2 (Catalogue of HII regions) | - | 300    | 300 |
| PK (Catalogue of Galactic Planetary Nebulae) | - | 1 500  | 1 500 | 
| Abell (Catalogue of Abell Clusters of Galaxies) | - | 2 700  | 2 700 |
| Hickson Compact Group (Hickson Compact Group, cluster of galaxies) | - | 100    | 100 |
| Comets | ~1000 | ~1000  | ~1000 |
| Minor Planets | 500 | 10 000 | 10 000 |
| WDS (The Washington Visual Double Star Catalog) | - | 120 000 | 120 000 | 
| Brightest Double Stars | - | 2 300 | 2 300 |

# Installation
Download the version you need from the assets. Install it on Android device directly (allowing installation of third-party APKs) or via adb (android debug bridge)
```shell
adb install path_to_apk
```
Run the application. The app will warn that it misses data files (expansion pack and patch). Go to Settings / Downloads and install data files by long-pressing on `Expansion file` and `Patch expansion file`.

The .apk files in the assets are signed with the same certificate that is used for signing DSO Planner application in Google Play Store (https://play.google.com/store/apps/details?id=com.astro.dsoplanner). To check the signature execute
```shell
apksigner verify --print-certs path_to_apk
```

# Building
I recommend to use release version, it is faster a bit as it is optimised by proguard
## Using Android studio
* Import the project. 
* Select the Build Variant (version and release)
  * three versions - Pro, Plus, Basic
  * 2 releases - release and debug

## Using CLI on Linux machine
Download `sdk-manager` from the bottom of the page https://developer.android.com/studio (say, named `commandlinetools-linux-11076708_latest.zip`). Execute the following commands:
```shell
git clone dsoplanner_url
unzip commandlinetools-linux-11076708_latest.zip
sudo apt install openjdk-17-jdk
mkdir -p ~/Android/Sdk
cd ~/Android/Sdk
~/cmdline-tools/bin/sdkmanager --install 'build-tools;30.0.2' 'platform-tools' 'platforms;android-30' 'tools' --sdk_root=.
# replace /path/to/home/ with your specific path
echo sdk.dir=/path/to/home/Android/Sdk > ~/dsoplanner/local.properties
cd ~/dsoplanner
/usr/bin/env sh gradlew clean
/usr/bin/env sh gradlew assemblePlusDebug
```
To find an apk
```shell
find . -name "*.apk"
```
Possible build variants
```shell
/usr/bin/env sh gradlew assembleProDebug
/usr/bin/env sh gradlew assembleProRelease
/usr/bin/env sh gradlew assemblePlusDebug
/usr/bin/env sh gradlew assemblePlusRelease
/usr/bin/env sh gradlew assembleBasicDebug
/usr/bin/env sh gradlew assembleBasicRelease
```