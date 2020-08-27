# Capentory Android App

Capentory is a system that allows our school to perform an inventory through a single platform. Our system will automatically take care of all edge cases. The system consists out of a server and an Android app that communicate through a JSON REST API. The user only needs to scan barcodes via the app. All barcodes are already present on all items in the organization due to legal obligations. There is an app showcase avaliable on [YouTube](https://www.youtube.com/watch?v=ktoLtNBN13E).

## Technical Details

The apps API Level is 21 (= Android 5.0 'Lollipop'). The app uses AndroidX and Jetpack libraries. Before reading the remaining documentation you should be familiar with basic Android development.

## App-Architecture 

The App makes use of MVVM-Architecture with an additional abstracted layer. MVVM generally includes three logical components:

* View -> Responsible for displaying the current data to the user. This layer should contain as little logic as possible.
* ViewModel -> Responsible for performing all the logic behind the scenes. 
* Repository -> Responsible for fetching and parsing the data.

Each component gets its data from the next higher level. The "data" is encapsulated into a wrapper-object that stores state information (INITIALIZED, SUCCESS, ERROR, FETCHING). That wrapper-object is further encapsulated into a LiveData-Object that is observable. The most primitive L

![MVVM according to Google](https://developer.android.com/topic/libraries/architecture/images/final-architecture.png)
          
We extracted this general MVVM logic into super classes:

* **NetworkFragment.java** for the abstracted **View**
* **NetworkViewModel.java** for the abstracted **ViewModel**
* **NetworkRepository.java** for the abstracted **Repository**

With this architecture in place it is quite easy to add additional features and locating bugs is much faster.


### Navigation

Navigation was implemented via the new Navigation Components library.

