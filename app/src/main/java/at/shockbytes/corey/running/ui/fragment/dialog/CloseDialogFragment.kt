package at.shockbytes.corey.running.ui.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import at.shockbytes.corey.running.R


/**
 * @author  Martin Macheiner
 * Date:    03.10.2017.
 */

class CloseDialogFragment : DialogFragment() {

    private var listener: ((stop: Boolean) -> Unit)? = null

    fun setOnCloseItemClickedListener(listener: ((stop: Boolean) -> Unit)): CloseDialogFragment {
        this.listener = listener
        return this
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("Stop running")
                .setMessage("Do you want to continue the run in the background")
                .setPositiveButton("Continue", { _, _ ->
                    listener?.invoke(false)
                })
                .setNegativeButton("Stop", { _, _ ->
                    listener?.invoke(true)
                })
                .create()
    }

    companion object {

        fun newInstance(): CloseDialogFragment {
            val fragment = CloseDialogFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
