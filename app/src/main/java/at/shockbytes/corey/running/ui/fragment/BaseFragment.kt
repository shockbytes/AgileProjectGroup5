package at.shockbytes.corey.running.ui.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import at.shockbytes.corey.running.core.RunningApp
import at.shockbytes.corey.running.dagger.AppComponent
import butterknife.ButterKnife
import butterknife.Unbinder

/**
 * @author  Martin Macheiner
 * Date:    29.11.2017.
 */
abstract class BaseFragment : Fragment() {

    private var unbinder: Unbinder? = null

    abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectToGraph((activity?.application as RunningApp).appComponent)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        unbinder = ButterKnife.bind(this, view)
        setupViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder?.unbind()
    }

    protected abstract fun setupViews()

    protected abstract fun injectToGraph(appComponent: AppComponent)

    @JvmOverloads
    protected fun showSnackbar(text: String, actionText: String,
                               showIndefinite: Boolean = false,  action: () -> Unit) {
        if (view != null) {
            val duration = if (showIndefinite) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG
            Snackbar.make(view!!, text, duration)
                    .setAction(actionText) {
                        action()
                    }.show()
        }
    }

    @JvmOverloads
    protected fun showSnackbar(text: String, showLong: Boolean = true) {
        if (view != null) {
            val duration = if (showLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT
            Snackbar.make(view!!, text, duration).show()
        }
    }

    protected fun showSnackbar(@StringRes text: Int, showLong: Boolean = true) {
        showSnackbar(getString(text), showLong)
    }

    @JvmOverloads
    protected fun showToast(text: String, showLong: Boolean = true) {
        val duration = if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast.makeText(context, text, duration).show()
    }

    @JvmOverloads
    protected fun showToast(text: Int, showLong: Boolean = true) {
        showToast(getString(text), showLong)
    }

    protected fun isPortrait(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

}