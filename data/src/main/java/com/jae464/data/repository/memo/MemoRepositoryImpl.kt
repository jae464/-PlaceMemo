package com.jae464.data.repository.memo

import com.jae464.data.dto.toMemo
import com.jae464.data.dto.toMemoDTO
import com.jae464.data.manager.ImageManager
import com.jae464.data.model.toMemo
import com.jae464.data.model.toMemoEntity
import com.jae464.data.repository.memo.local.MemoLocalDataSource
import com.jae464.data.repository.memo.remote.MemoRemoteDataSource
import com.jae464.domain.model.post.Memo
import com.jae464.domain.repository.MemoRepository

import javax.inject.Inject

class MemoRepositoryImpl @Inject constructor(
    private val memoLocalDataSource: MemoLocalDataSource,
    private val memoRemoteDataSource: MemoRemoteDataSource,
): MemoRepository {

override suspend fun getMemo(id: Long): Memo = memoLocalDataSource.getMemo(id).toMemo()

    override suspend fun saveMemo(memo: Memo): Long {
        return memoLocalDataSource.saveMemo(memo.toMemoEntity())
    }

    override suspend fun updateMemo(memo: Memo) {
        memoLocalDataSource.updateMemo(memo.toMemoEntity())
    }

    override suspend fun updateMemoOnRemote(userId: String, memo: Memo) {
        memoRemoteDataSource.updateMemo(userId, memo.toMemoDTO(userId))
    }

    override suspend fun saveMemoOnRemote(userId: String, memo: Memo) {
        return memoRemoteDataSource.insertMemo(memo.toMemoDTO(userId))
    }

    override suspend fun getAllMemo(): List<Memo> {
        val memoList = mutableListOf<Memo>()
        memoLocalDataSource.getAllMemo().forEach { memoEntity ->
            memoList.add(memoEntity.toMemo())
        }
        return memoList
    }

    override suspend fun getAllMemoByUserOnRemote(uid: String): List<Memo> {
        val memoList = mutableListOf<Memo>()
        memoRemoteDataSource.getAllMemoByUser(uid).forEach {
            memoList.add(it.toMemo())
        }
        return memoList
    }

    override suspend fun getMemoByCategory(category: Int): List<Memo> {
        val memoList = mutableListOf<Memo>()
        memoLocalDataSource.getMemoByCategory(category).forEach {
//            memoList.add(memoEntityToMemo(it))
            memoList.add(it.toMemo())
        }
        return memoList
    }

    override suspend fun getMemoByTitle(title: String): List<Memo> {
        val memoList = mutableListOf<Memo>()
        memoLocalDataSource.getMemoByTitle(title).forEach { memoEntity ->
            memoList.add(memoEntity.toMemo())
        }
        return memoList
    }

    override suspend fun getMemoByContent(content: String): List<Memo> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMemo(id: Long) {
        memoLocalDataSource.deleteMemo(id)
    }

    override suspend fun deleteMemoOnRemote(userId: String, memoId: Long) {
        memoRemoteDataSource.deleteMemo(userId, memoId)
    }

    override suspend fun saveImages(memoId: Long, imagePathList: List<String>) {
        memoLocalDataSource.saveMemoImages(memoId, imagePathList)
    }

    override suspend fun saveImagesOnRemote(memoId: Long, imagePathList: List<String>) {
        TODO("Not yet implemented")
    }

    override fun getImagePathList(memoId: Long): List<String> {
        return memoLocalDataSource.getImagePathList(memoId)
    }


//    override fun saveImage(imageList: List<Bitmap>, memoId: Long) {
//        ImageManager.saveImage(imageList, memoId)
//    }
//
//    override suspend fun saveImageOnRemote(imageList: List<Bitmap>, imageUriList: List<String>) {
//        memoRemoteDataSource.saveImageOnRemote(imageList, imageUriList)
//    }
}