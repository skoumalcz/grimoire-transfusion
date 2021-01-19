<p align="center">
  <img src="art/logo.svg" width="128px" />
</p>
<p align="center">
    <a href="https://bintray.com/diareuse/grimoire/transfusion/"><img src="https://api.bintray.com/packages/diareuse/grimoire/transfusion/images/download.svg?version=latest" /></a>
</p>
<h1 align="center">Grimoire<sup>Transfusion</sup></h1>

Why Transfusion?

* **:)**

    Well you need some parts of your app to talk to each other.

### `Observer`

Observer works as a drop-in utility to bindable objects. The main benefit is that you don't need to
use heavy `ObservableField`s which collect listeners within them - often overlapping with other
`ObservableField`s.

With observer you're able to use native kotlin delegation for pretty syntax and improved
performance.

```kotlin
class MyBindableObject : Observer by Observer.getDefault() {

    @get:Bindable
    var myVariable by observable(Any(), BR.myVariable)
        private set

    fun onStateChanged() {
        // todo mutate myVariable
    }

}
```

### `Transfusion` + [`extensions`](transfusion-method/src/main/java/com/skoumal/grimoire/transfusion/method)

Transfusion offers unidirectional communication within a specific scope of items provided. One
object always serves as provider, other as consumer. Default implementation allows multiple
consumers, however you should be aware that this can bring _undesired consequences_.

```kotlin
data class MyState(
    val isExpanded: Boolean = false
)

class MyViewModel : ViewModel(), Transfusion<MyState> by Transfusion.getDefault() {

    private var state = MyState()

    fun onExpand() {
        state = state.copy(isExpanded = true)
        state.offer()
    }

}

class MyFragment : Fragment() {

    private val viewModel: MyViewModel by viewModels()

    override fun onViewCreated(/**/) {
        lifecycleScope.launch {
            viewModel.openFlow().collect { /* it: MyState -> */ }
        }
    }

}
```

> What's shown above is a very raw example. However that's not the way you're gonna be using
transfusion since we provide [`extensions`](transfusion-method/src/main/java/com/skoumal/grimoire/transfusion/method)
Here's how to use them.

```kotlin
class MyViewModel : ViewModel(), Transfusion<Cell> by Transfusion.getDefault() {

    fun onExpand() = MyFragment.Expand().offer()

}

class MyFragment : Fragment() {

    private val viewModel: MyViewModel by viewModels()
    private lateinit var consumer: TransfusionFlowConsumer<Cell>

    override fun onViewCreated(/**/) {
        consumer = viewModel.openFlow().consumeIn(this)
    }

    override fun onDestroyView() {
        consumer.stop()
    }

    private fun doExpand() {}

    class Expand : Cell(), InFragment {
        override fun invoke(fragment: Fragment) {
            if(fragment !is MyFragment) return
            fragment.doExpand()
        }
    }

}
```

> Notice that by using this pattern of having `Cell` classes _inside_ the Fragment, we are able to
use private methods. This is very much beneficial for keeping consumers of `MyFragment` accidentally
calling public method that's not supposed to be called directly.

> Also you can use `InActivity`, `InContext`, `InFragment`. Consult the documentation for each
interface to learn the caveats of using each of them.

### `LiveTransfusionHost`

You might be consuming `LiveData` outside lifecycle and accidentally leaking precious resources.
Then the easy way out is to use `LiveTransfusionHost`. It gets implemented just as easy as the rest
of the utilities mentioned here and is just as powerful.

```kotlin
class MyViewModel : ViewModel(), LiveTransfusionHost by LiveTransfusionHost.getDefault() {

    private val databaseWindow: LiveData<List<String>> = getFromSomewhere()

    init {
        databaseWindow.transfusion { /* it: List<String> -> */ }
    }

    override fun onCleared() {
        clearTransfusions()
    }

}
```

Logo by <a href="https://www.flaticon.com/authors/smalllikeart" title="smalllikeart">smalllikeart</a>
