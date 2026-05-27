plugins {


    id("com.android.library")


    id("org.jetbrains.kotlin.android")


}





android {


    namespace = "com.pdfadder.feature.merge"


    compileSdk = 34





    defaultConfig {


        minSdk = 24





        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        consumerProguardFiles("consumer-rules.pro")


    }





    buildTypes {


        release {


            isMinifyEnabled = false


            proguardFiles(


                getDefaultProguardFile("proguard-android-optimize.txt"),


                "proguard-rules.pro"


            )


        }


    }


    compileOptions {


        sourceCompatibility = JavaVersion.VERSION_1_8


        targetCompatibility = JavaVersion.VERSION_1_8


    }


    kotlinOptions {


        jvmTarget = "1.8"


    }


    buildFeatures {


        viewBinding = true


    }


}





dependencies {


    implementation(project(":core-api"))


    implementation(files("libs/fitz-1.27.1.jar"))
    implementation("androidx.core:core-ktx:1.12.0")


    implementation("androidx.appcompat:appcompat:1.6.1")


    implementation("androidx.activity:activity-ktx:1.8.2")


    implementation("androidx.fragment:fragment-ktx:1.6.2")


    implementation("androidx.recyclerview:recyclerview:1.3.2")


    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")


    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")


    implementation("com.google.android.material:material:1.9.0")


}