package com.example.swiftycompanion

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

// User class implementing Parcelable
data class User(
    val id: Int,
    val email: String,
    val login: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("usual_full_name") val usualFullName: String,
    @SerializedName("usual_first_name") val usualFirstName: String,
    val url: String,
    val phone: String?,
    val displayname: String,
    val kind: String,
    val image: Image42,
    @SerializedName("staff?") val isStaff: Boolean,
    @SerializedName("correction_point") val correctionPoint: Int,
    @SerializedName("pool_month") val poolMonth: String,
    @SerializedName("pool_year") val poolYear: String,
    val location: String?,
    val wallet: Int,
    @SerializedName("anonymize_date") val anonymizeDate: String,
    @SerializedName("data_erasure_date") val dataErasureDate: String?,
    @SerializedName("alumni?") val isAlumni: Boolean,
    @SerializedName("active?") val isActive: Boolean,
    val groups: List<String>,
    @SerializedName("cursus_users") val cursusUsers: List<CursusUser>,
    @SerializedName("projects_users") val projectsUsers: List<ProjectUser>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(Image42::class.java.classLoader)!!,
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.createStringArrayList()!!,
        parcel.createTypedArrayList(CursusUser.CREATOR)!!,
        parcel.createTypedArrayList(ProjectUser.CREATOR)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(email)
        parcel.writeString(login)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(usualFullName)
        parcel.writeString(usualFirstName)
        parcel.writeString(url)
        parcel.writeString(phone)
        parcel.writeString(displayname)
        parcel.writeString(kind)
        parcel.writeParcelable(image, flags)
        parcel.writeByte(if (isStaff) 1 else 0)
        parcel.writeInt(correctionPoint)
        parcel.writeString(poolMonth)
        parcel.writeString(poolYear)
        parcel.writeString(location)
        parcel.writeInt(wallet)
        parcel.writeString(anonymizeDate)
        parcel.writeString(dataErasureDate)
        parcel.writeByte(if (isAlumni) 1 else 0)
        parcel.writeByte(if (isActive) 1 else 0)
        parcel.writeStringList(groups)
        parcel.writeTypedList(cursusUsers)
        parcel.writeTypedList(projectsUsers)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User = User(parcel)
        override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
    }
}

// Image42 class implementing Parcelable
data class Image42(
    val versions: Versions
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable<Versions>(Versions::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(versions, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Image42> {
        override fun createFromParcel(parcel: Parcel): Image42 = Image42(parcel)
        override fun newArray(size: Int): Array<Image42?> = arrayOfNulls(size)
    }
}

data class Versions(
    val large: String
) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()!!)
    override fun writeToParcel(parcel: Parcel, flags: Int) = parcel.writeString(large)
    override fun describeContents(): Int = 0
    companion object CREATOR : Parcelable.Creator<Versions> {
        override fun createFromParcel(parcel: Parcel): Versions = Versions(parcel)
        override fun newArray(size: Int): Array<Versions?> = arrayOfNulls(size)
    }
}

// CursusUser class implementing Parcelable
data class CursusUser(
    val id: Int,
    val level: Double,
    val grade: String?,
    val cursus: Cursus,
    val skills: List<Skill>,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readString(),
        Cursus(parcel.readInt(), parcel.readString() ?: ""),
        mutableListOf<Skill>().apply {
            parcel.readTypedList(this, Skill.CREATOR) // Correct way to read a list of Parcelable
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeDouble(level)
        parcel.writeString(grade)
        parcel.writeInt(cursus.id)
        parcel.writeString(cursus.name)
        parcel.writeTypedList(skills)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CursusUser> {
        override fun createFromParcel(parcel: Parcel): CursusUser = CursusUser(parcel)
        override fun newArray(size: Int): Array<CursusUser?> = arrayOfNulls(size)
    }
}

data class Skill(
    val id: Int,
    val name: String?,
    val level: Double
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeDouble(level)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Skill> {
        override fun createFromParcel(parcel: Parcel): Skill = Skill(parcel)
        override fun newArray(size: Int): Array<Skill?> = arrayOfNulls(size)
    }
}

// Project42 class implementing Parcelable
data class ProjectUser(
    val id: Int,
    val final_mark: Int?,
    val project: ProjectName,
    val marked_at: String?,
    val status: String,
    val cursus_ids: List<Int>
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        ProjectName(parcel.readString() ?: ""),
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.createIntArray()?.toList() ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeValue(final_mark)
        parcel.writeString(project.name)
        parcel.writeString(marked_at)
        parcel.writeString(status)
        parcel.writeIntArray(cursus_ids.toIntArray())
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ProjectUser> {
        override fun createFromParcel(parcel: Parcel): ProjectUser = ProjectUser(parcel)
        override fun newArray(size: Int): Array<ProjectUser?> = arrayOfNulls(size)
    }
}

data class ProjectName(
    val name: String
)


data class Cursus(
    val id: Int,
    val name: String
)
