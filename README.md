#Reactive-Ether library
####Broadcast and observe (share) resource objects

Reactive-Ether was created to easily share / persist non-serializable objects between Android
[Activities](http://developer.android.com/reference/android/app/Activity.html) and
[Fragments](http://developer.android.com/reference/android/app/Fragment.html) where serializing resources in the
[Bundle](http://developer.android.com/reference/android/os/Bundle.html) is the standard mechanism during device
configuration changes and Fragment/Activity communication.