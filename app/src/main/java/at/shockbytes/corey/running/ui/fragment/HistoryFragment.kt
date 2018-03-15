package at.shockbytes.corey.running.ui.fragment


import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

import javax.inject.Inject

import at.shockbytes.corey.running.R
import at.shockbytes.corey.running.adapter.RunAdapter
import at.shockbytes.corey.running.dagger.AppComponent
import at.shockbytes.corey.running.running.Run
import at.shockbytes.corey.running.storage.StorageManager
import at.shockbytes.util.adapter.BaseAdapter
import butterknife.BindView


class HistoryFragment : BaseFragment(), BaseAdapter.OnItemClickListener<Run> {

    @Inject
    protected lateinit var storageManager: StorageManager

    @BindView(R.id.fragment_history_rv)
    protected lateinit var recyclerView: RecyclerView

    @BindView(R.id.fragment_history_empty)
    protected lateinit var txtEmpty: TextView


    override val layoutId = R.layout.fragment_history

    override fun setupViews() {
        val adapter = RunAdapter(context, storageManager.runs)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        adapter.onItemClickListener = this

        if (adapter.itemCount == 0) {
            txtEmpty.animate().alpha(1f).start()
        }
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    override fun onItemClick(t: Run, v: View) {

        fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.main_content, HistoryDetailFragment.newInstance(t))
                .addToBackStack(null)
                .commit()
    }

    companion object {

        fun newInstance(): HistoryFragment {
            val fragment = HistoryFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}
