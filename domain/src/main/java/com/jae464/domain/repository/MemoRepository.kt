package com.jae464.domain.repository

import com.jae464.domain.model.post.Memo

interface MemoRepository {
    suspend fun getMemo(id: Long): Memo

    suspend fun saveMemo(memo: Memo): Long
    suspend fun saveMemoOnRemote(userId: String, memo: Memo)

    suspend fun updateMemo(memo: Memo)
    suspend fun updateMemoOnRemote(userId: String, memo: Memo)

    suspend fun getAllMemo(): List<Memo>
    suspend fun getAllMemoByUserOnRemote(uid: String): List<Memo>

    suspend fun getMemoByCategory(category: Int) : List<Memo>
    suspend fun getMemoByTitle(title: String): List<Memo>
    suspend fun getMemoByContent(content: String): List<Memo>

    suspend fun deleteMemo(id: Long)
    suspend fun deleteMemoOnRemote(userId: String, memoId: Long)

//    fun saveImage(imageList: List<Bitmap>, memoId: Long)
//    suspend fun saveImageOnRemote(imageList: List<Bitmap>, imageUriList: List<String>)

    fun saveImage(imagePathList: List<String>, memoId: Long)
    suspend fun saveImageOnRemote(imagePathList: List<String>, imageUriList: List<String>)

}