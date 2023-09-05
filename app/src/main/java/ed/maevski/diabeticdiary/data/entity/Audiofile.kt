package ed.maevski.diabeticdiary.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import ed.maevski.diabeticdiary.data.entity.Audiofile.AudioFileConst.NOT_UPLOAD_AUDIOFILE

@Entity(
    tableName = "audiofiles",
    primaryKeys = ["nameFile"],
    indices = [Index(value = ["nameFile"], unique = true)]
)
data class Audiofile(
    @ColumnInfo(name = "nameFile")  val nameFile: String,
    @ColumnInfo(name = "path") val path: String,
    @ColumnInfo(name = "state") val state: Int,
    @ColumnInfo(name = "timestampUploadInMillis") val timestampUploadInMillis: Long = 0,
    @ColumnInfo(name = "stateUpload") val stateUpload: Int = NOT_UPLOAD_AUDIOFILE
) {
    object AudioFileConst {
        const val CREATE_AUDIOFILE = 1
        const val CREATED_AUDIOFILE = 2
        const val MARK_TO_DELETE_AUDIOFILE =3
        const val DELETED_AUDIOFILE = 4
        const val NOT_UPLOAD_AUDIOFILE = 10
        const val UPLOAD_AUDIOFILE = 11
        const val ERROR_UPLOAD_AUDIOFILE = 12
        const val CONFIRM_UPLOAD_AUDIOFILE = 14
    }
}