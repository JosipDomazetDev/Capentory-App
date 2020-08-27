# Capentory Android App

Capentory is a system that allows our school to perform an inventory through a single platform. Our system will automatically take care of all edge cases. The system consists out of a server and an Android app that communicate through a JSON REST API. The user only needs to scan barcodes via the app. All barcodes are already present on all items in the organization due to legal obligations. There is an app showcase avaliable on [YouTube](https://www.youtube.com/watch?v=ktoLtNBN13E).

## Technical Details

The apps API Level is 21 (= Android 5.0 'Lollipop'). The app uses AndroidX and Jetpack libraries. Before reading the remaining documentation you should be familiar with basic Android development.

### App-Architecture 

The architecture is based on this [official article](https://developer.android.com/jetpack/guide) that is worth reading. The app makes use of MVVM-Architecture with an additional abstracted layer. MVVM generally includes three logical components:

* View -> Responsible for displaying the current data to the user. This layer should contain as little logic as possible.
* ViewModel -> Responsible for performing all the logic behind the scenes. 
* Repository -> Responsible for fetching and parsing the data.

Each component gets its data from the next higher level. The "data" is encapsulated into a wrapper-object that stores state information (INITIALIZED, SUCCESS, ERROR, FETCHING). That wrapper-object is further encapsulated into a **LiveData**-object that is observable. The View-component simply **observes** that LiveData-Object. If the LiveData-object changes a callback function will be executed in the View-component hence ensuring that the user sees up-to-date data. 

![MVVM according to Google](https://developer.android.com/topic/libraries/architecture/images/final-architecture.png)
          
We extracted this general MVVM logic into super classes:

* **NetworkFragment.java** for the abstracted **View**
* **NetworkViewModel.java** for the abstracted **ViewModel**
* **NetworkRepository.java** for the abstracted **Repository**

With this architecture in place it is quite easy to add additional features and locating bugs is much faster.


### Navigation

Navigation was implemented via the new Navigation Components library.

### Scanning

Scanning barcodes is the most essential part of this app.

* Camera-Scan: Implemented via Google Mobile Vision API, heaviliy optimized: Limit the barcode-formats in the settings to squeeze out even more performance. 
* Text-Scan: Implemented via Google Mobile Vision API, heaviliy optimized: Supports different modes including a barcode mode. The barcode mode locks in the read result after reading it several times and automatically tries to correct common errors. Reading a result multiple times is key to ensure reliability since text recognization itself is not the most reliable technology.
* Zebra-Scan: Makes use of built-in barcode reader of Zebra devices. DataWedge is an app that runs in the background of Zebra devices. If the built-in barcode reader is activated the result is dispatched as a **Broadcast** by DataWedge. Capentory listens to that Broadcast via a **BroadcastReceiver**. This scan is extremely fast and reliable and by far the best option. 

### Authentication

The authentication works via API tokens that are encrypted and stored in the SharedPreferences. This process is quite sophisticated and makes use of [this utility class](https://gist.github.com/Diederikjh/36ae22d5fde9d8f671a70b5d8cada90e).

### HTTPS

HTTPS is the prefered way to communicate with the server. The app also supports self-signed certificates for HTTPS. HTTP is also supported although you should only use it if you absolutely must! I repeat, do not use HTTP unless you have a really really really good reason to do so. 

### General Workflow

A general inventory might look like this:

1. The user starts the app and is greeted by the home screen.
2. The user logs in.
3. The user chooses a database-view and inventory.
4. The user chooses a room.
5. The user receives a list of items for the room. The user now needs to scan all barcodes in the room to validate the list item by item. 
6. **Optional:** User decides to edit the attributes of an item. 
7. If he has scanned all barcodes in the list he can send his changes to the server. The inventory is this room is completed. Go to Step 4 again to continue with the next room. 

### Ending

This crash course should give you enough information to make modifications to the app.






