#Reactive-Ether library
####Broadcast and observe (share) resource objects

Reactive-Ether was created to easily share / persist non-serializable objects between Android
[Activities](http://developer.android.com/reference/android/app/Activity.html) and
[Fragments](http://developer.android.com/reference/android/app/Fragment.html) where serializing resources in the
[Bundle](http://developer.android.com/reference/android/os/Bundle.html) is the standard mechanism during device
configuration changes and Fragment/Activity communication.

[JavaDocs](http://manimaul.github.io/reactive-ether/docs/javadoc/com/willkamp/ether/Ether.html)

####Gradle

```gradle

allprojects {
    repositories {
        jcenter()
        maven {
            url  "http://dl.bintray.com/manimaul/maven"
        }
    }
}


dependencies {
    compile 'com.willkamp.ether:reactive-ether:0.4'
    compile 'io.reactivex:rxjava:1.1.0'
    compile 'io.reactivex:rxandroid:1.1.0'
}

```

####Simple example persisting a resource during an Activity configuration change

```java
public class MyActivity extends AppCompatActivity {

    private static final String CONTROLLER_KEY = Ether.createUniqueKey();

    private MyActivityController _myActivityController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null && savedInstanceState.containsKey(CONTROLLER_KEY)) {
            _myActivityController = Ether.getResourceWithKey(savedInstanceState.getString(CONTROLLER_KEY));
        } else {
            _myActivityController = new MyActivityController();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Ether.hold(CONTROLLER_KEY, _myActivityController);
    }
}
```
