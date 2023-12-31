# APK Analyzer
This is an Android app written in kotlin to scan APK files for any vulnerability using Mobile Security Framework (MobSF). 
<br>
<br>
## Mobile Security Framework
Mobile Security Framework (MobSF) is an automated, all-in-one mobile application (Android/iOS/Windows) pen-testing, malware analysis and security assessment framework capable of performing static and dynamic analysis. MobSF support mobile app binaries (APK, XAPK, IPA & APPX) along with zipped source code and provides REST APIs for seamless integration with your CI/CD or DevSecOps pipeline. The Dynamic Analyzer helps you to perform runtime security assessment and interactive instrumented testing.
<br>
<br>
## Description
In this project we are going to develop a mobile security tool that will be used to download and analyze
Android application packages (APKs). The tool will be based on the Mobile
Security Framework (MobSF) and will use a Representational State Transfer (REST) API to
generate reports of the analysis.

The first step in this project is to install the necessary software, including MobSF and any
required libraries. The MobSF software will be used to perform static and dynamic analysis on
the APKs. The REST API will be used to create and store the reports on the analysis.

Once the software is installed, the tool will be configured to download APKs from evozi using web scraping methods.
This tool also parse the metadata of the APKs, including the package name, version, target SDK version, and permissions.

The tool will then be used to perform static and dynamic analysis on the downloaded
APKs. This will include decompiling the APKs, analyzing the code for any vulnerable points,
and running the APKs in an emulator to observe the behavior of the application.
Once the analysis is complete, the tool will use the REST API to generate a report of the
analysis. This report will include details such as the application’s package name, target SDK
version, permissions, and any vulnerabilities that were discovered.
<br>
<br>
## Application UI

<p float="middle">
<img src="./Screenshots/main.png"  width=200>
<img src="./Screenshots/signup.png"  width=200>
<img src="./Screenshots/login.png"  width=200>
<img src="./Screenshots/apkdownload.png"  width=200>
</p>

## MobSF Installation
<h3>Easy Setup</h3>
For easy setup you just need to execute these two commands:

```bash
docker pull opensecurity/mobile-security-framework-mobsf
docker run -it --rm -p 8000:8000 opensecurity/mobile-security-framework-mobsf:latest
```
If you need Dynamic Analysis, do not setup MobSF inside Docker or Virtual Machine instead use this steps:
<br>
<br>
<h3>Requirements</h3>
First of all you need to install requirements for your appropriate operating system.<br>
check this link: https://mobsf.github.io/docs/#/requirements 

<br>
<br>
<h3>Installation steps</h3>
Please make sure that all the requirements mentioned are installed first.<br>
<br>

Linux/Mac
```bash
git clone https://github.com/MobSF/Mobile-Security-Framework-MobSF.git
cd Mobile-Security-Framework-MobSF
./setup.sh
```
Windows
```bash
git clone https://github.com/MobSF/Mobile-Security-Framework-MobSF.git
cd Mobile-Security-Framework-MobSF
setup.bat
```
<br>
<h3>Running MobSF</h3>
Linux/Mac

```bash
./run.sh 127.0.0.1:8000
```

Windows
```bash
run.bat 127.0.0.1:8000
```
<br>
<img src="./Screenshots/mobsf.png"  width=50%>
<br>
<h2>Android App Structure</h2>
This application consist of 5 different activities.
LaunchScreen Activity , Sign up Activity , Sign in Activity , Main Activity , APKDownload Activity.
<br>
<h3>LaunchScreen Activity</h3>
A splash screen is displayed briefly when the app is launched then after 3 seconds the activity changes to Sign in page.<br>

```kotlin
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000) // 3000 is the delayed time in milliseconds.
```
<br>
<h3>Sign in Activity</h3>
This activity includes a pair of text inputs for email and password and a button to send the data to firebase database.
there is also a text under the sign in button to change the activity to sign up page.
<br>
<br>
According to this code snippet if the provided credentials by the user is accepted thus user logs in and activity changes to main activity.

```kotlin
firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
      if (it.isSuccessful) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
```
<br>
<img src="./Screenshots/firebase.png"  width=50%>
<br>
<h3>Sign up Activity</h3>
This activity includes three text inputs for email and password and password repeat and a button to send the data to firebase database.
there is also a text under the sign up button to change the activity to sign in page.
<br>
<br>
According to this code snippet firebase registers an account with the provided credentials by user then the activity changes to Sign in page.

```kotlin
        firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                if (it.isSuccessful) {
                        val intent = Intent(this, SignInActivity::class.java)
                        startActivity(intent)
                } 
                else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                }
```
<br>
<h3>Main Activity</h3>
This activity include main features of the app for example: choosing a file from storage , Parsing APK Metadata such as package name or version , Intracting with REST API endpoints using OkHttpClient for uploading files or static analyzing or downloding reports , handling user permissions and etc.
<br>
<br>
<h3>APK Download Activity</h3>
The purpose of the ApkDownload activity is to provide functionality related to downloading APK files using web scraping methods.
We used Jsoup and webdriver for intracting with the evozi.app website in the background and finding the download link for the provided package name.

```kotlin
    private fun processPackageName(packageName: String) {
        WebDriverManager.chromedriver().setup()

        val options = ChromeOptions()
        options.addArguments("--headless")
        options.addArguments("--disable-gpu")
        options.addArguments("--no-sandbox")
        options.addArguments("--disable-dev-shm-usage")

        val driver = ChromeDriver(options)

        driver.get("https://apps.evozi.com/apk-downloader/")

        val inputElement = driver.findElement(By.id("IjUggSZkjlWSRZqbo"))
        inputElement.sendKeys(packageName)

        val updatedHtml = driver.pageSource

        val document = Jsoup.parse(updatedHtml)

        // Extract information from the parsed document as needed
        val downloadButton = document.select("button#TZBFjPrMGb")
        if (downloadButton.isNotEmpty()) {
            val buttonElement = driver.findElement(By.id("TZBFjPrMGb"))
            buttonElement.click()
        } else {
            println("Download button not found.")
        }

        driver.quit()
    }
```
<br>
<br>

## Dynamic Analyzer

Android Emulator image with Google Play Store is considered as production image and you cannot use that with MobSF. Create an Android Virtual Device (AVD) without Google Play Store. Do not start the AVD from Android Studio, instead start the AVD with writable system using emulator command line options.
For that, add your Android SDK emulator directory to PATH.
<br>
<h3>Run Android Virtual Device (AVD)</h3>
Run an AVD before starting MobSF using emulator command line options.

```bash
$ emulator -avd <non_production_avd_name> -writable-system -no-snapshot
```
<br>
Everything will be configured automatically at runtime. MobSF requires AVD version 5.0 to 9.0 for dynamic analysis. We recommend using Android 7.0 and above.
Only Android images upto API 28 are supported!
<br>
<br>
<p float="middle">
<img src="./Screenshots/mobsfdynamic.png"  width=40%>
<img src="./Screenshots/mobsfemu.png"  width=40%>
</p>
