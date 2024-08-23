package com.example.quranrecitation.room

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "audioRecords")
@Parcelize
data class AudioRecord(
    var filename: String,
    var filePath: String,
    var timestamp: Long,
    var duration: String,
    var ampsPath: String,
) : Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @Ignore
    var isChecked: Boolean = false
}
