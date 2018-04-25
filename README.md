Voice authentication for Voice assistants
===================================

Introduction
------------
This project is an implementation of two protections for voice assistatns.
The application simulates the process of using voice assistants and adds the two protections into the process.
The two simulated process are:

A: only identify the speaker.
B: includes speak identification and challenge-response to verify the speaker.

Before using the system, the user must enroll in the speaker recognition system used in the application.
The application utilizes the existing speaker recognition (MS azure speaker recognition system) to
extract user's voice features and compares the voiceprint with enrolled ones.

Pre-requisites
--------------
- Android SDK v26
- Android Build Tools v26.0.2
- [MS azure speaker recognition system](https://azure.microsoft.com/en-us/services/cognitive-services/speaker-recognition/ "azure"): save it in assets/azure_credentials
- [Google cloud speech-to-text](https://cloud.google.com/speech-to-text/): save it in assets/credentials.json


Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.

License
-------

