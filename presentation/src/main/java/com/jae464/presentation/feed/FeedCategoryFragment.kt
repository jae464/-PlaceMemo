package com.jae464.presentation.feed

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.paging.map
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.jae464.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import com.jae464.presentation.R
import com.jae464.presentation.databinding.FragmentFeedCategoryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FeedCategoryFragment : BaseFragment<FragmentFeedCategoryBinding>(R.layout.fragment_feed_category) {

    private val TAG: String = "FeedCategoryFragment"
    private var feedListAdapter: FeedListAdapter? = null
    private var listAdapter: FeedListAdapter? = null
    private val viewModel: FeedViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        feedListAdapter = FeedListAdapter(requireContext(), this::goToDetailPage, 0)
        listAdapter = FeedListAdapter(requireContext(), this::goToDetailPage, 1)
        binding.feedRecyclerView.adapter = feedListAdapter

        initObserver()
        initListener()

        // Firebase 메모 불러오기 테스트
        // viewModel.getAllMemoByUser(FirebaseAuth.getInstance().currentUser!!.uid)
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.memoList.collectLatest { pagingData ->
                    feedListAdapter?.submitData(pagingData)
                    listAdapter?.submitData(pagingData)
                }
            }
        }
    }

    private fun initListener() {
        binding.chipTypeFeedType.setOnCheckedStateChangeListener { group, checkedIds ->
            Log.d(TAG, checkedIds.toString())

            when (checkedIds[0]) {
                R.id.chip_type_card_view -> {
                    viewModel.viewType.value = "card"
                    binding.feedRecyclerView.adapter = feedListAdapter
                    binding.feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                }
                R.id.chip_type_grid_view -> {
                    viewModel.viewType.value = "card"
                    binding.feedRecyclerView.adapter = feedListAdapter
                    binding.feedRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                }
                R.id.chip_type_list_view -> {
                    viewModel.viewType.value = "list"
                    binding.feedRecyclerView.adapter = listAdapter
                    binding.feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                }
            }
        }
    }

    private fun goToDetailPage(memoId: Int) {
//        val action = FeedCategoryFragmentDirections.actionFeedToDetailMemo(memoId)
//        findNavController().navigate(
//            action
//        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
    }
}