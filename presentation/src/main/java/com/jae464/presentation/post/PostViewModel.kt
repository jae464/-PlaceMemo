package com.jae464.presentation.post

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.jae464.domain.model.post.Category
import com.jae464.domain.model.post.Memo
import com.jae464.domain.model.post.Region
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: com.jae464.domain.repository.MemoRepository,
    private val addressRepository: com.jae464.domain.repository.AddressRepository,
    private val loginRepository: com.jae464.domain.repository.LoginRepository
) : ViewModel() {

    val TAG = "PostViewModel"

    private val _address = MutableLiveData<Region?>()
    val address: LiveData<Region?> get() = _address

    private var memoId: Long = -1
    private var user = FirebaseAuth.getInstance().currentUser

    private val _memo = MutableLiveData<com.jae464.domain.model.post.Memo>()
    val memo: LiveData<com.jae464.domain.model.post.Memo> get() = _memo

    private val _isDone = MutableLiveData<Boolean>()
    val isDone: LiveData<Boolean> get() = _isDone

//    private val imagePathList = mutableListOf<String>()

    @SuppressLint("SimpleDateFormat")
    fun saveMemo(id: Long, title: String, content: String, latitude: Double, longitude: Double, category: Category, imageUriList: List<String>) {
        viewModelScope.launch {
            val region = _address.value

//            for (i in imageUriList.indices) {
//                Log.d(TAG, i.toString())
//                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//                val imageFileName = "${user?.uid}_${timeStamp}_${i}.jpg"
//                imagePathList.add(imageFileName)
//            }

            val memo = Memo(
                id, title, content, latitude, longitude,
                category,
                region?.area1 ?: "",
                region?.area2 ?: "",
                region?.area3 ?: "",
                imageUriList
            )

            memoId = repository.saveMemo(memo)
            saveImage(memoId, imageUriList)

            val newMemo = memo.copy(id = memoId)

            // 로그인이 되어있을 시, 파이어베이스 스토어에 저장
            if (user != null) {
                Log.d(TAG, user.toString())
                repository.saveMemoOnRemote(user?.uid.toString(), newMemo)
            }

            _isDone.postValue(true)

        }
    }

    fun updateMemo(title: String, content: String, category: Category, imageUriList: List<String>) {
        val beforeMemo = memo.value ?: return
        val newMemo = com.jae464.domain.model.post.Memo(
            beforeMemo.id, title, content, beforeMemo.latitude, beforeMemo.longitude,
            category, beforeMemo.area1, beforeMemo.area2, beforeMemo.area3, imageUriList
        )

        Log.d(TAG, newMemo.toString())

        viewModelScope.launch {
            // Local Room update
            repository.updateMemo(newMemo)
            saveImage(newMemo.id, imageUriList)

            // Remote Firebase update
            if (user != null) {
                repository.updateMemoOnRemote(user?.uid.toString(), newMemo)
                // TODO Image Remote Update
            }

            _isDone.postValue(true)
        }
    }

    private fun saveImage(memoId: Long, imageUriList: List<String>) {

        if (imageUriList.isEmpty()) return
        viewModelScope.launch {
            Log.d(TAG, "이미지 저장 시작")
            repository.saveImages(memoId, imageUriList)
            Log.d(TAG, "이미지 저장 완료")
        }

        // 네트워크 연결되어 있을 시 FireBase Store 에 저장
        saveImageOnRemote(imageUriList)
    }

    fun getAddress(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val address = addressRepository.getAddress(longitude, latitude)
            _address.postValue(address)
        }
    }

    fun getAddressName(region: Region?): String {
        return addressRepository.addressToString(region)
    }

    fun getMemo(memoId: Long) {
        viewModelScope.launch {
            val memo = repository.getMemo(memoId)
            _memo.postValue(memo)

//            memo.imageUriList?.forEach {
//                imagePathList.add(it)
//            }
        }
    }
    private fun saveImageOnRemote(saveImageList: List<String>) {
        viewModelScope.launch {
//            Log.d(TAG, imagePathList.toString())
//            repository.saveImageOnRemote(saveImageList, imageFileNameList)
        }
    }

}