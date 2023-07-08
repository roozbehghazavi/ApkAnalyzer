package com.codingstuff.loginandsignup

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import io.github.bonigarcia.wdm.WebDriverManager
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions


class ApkDownload : AppCompatActivity() {

    private lateinit var packageNameEditText: EditText
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apk_download)

        packageNameEditText = findViewById(R.id.inputText)
        button = findViewById(R.id.button)

        button.setOnClickListener {
            val packageName = packageNameEditText.text.toString()
            processPackageName(packageName)
        }
    }
    override fun onBackPressed() {
        // Perform any additional cleanup or navigation logic here
        // ...

        // Call finish() to close the current activity and return to the previous activity
        finish()
        super.onBackPressed()
    }

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
}