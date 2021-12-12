package com.example.snapshots.Fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.example.snapshots.R
import com.example.snapshots.SnapshotsApplication
import com.example.snapshots.databinding.FragmentAddBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.example.snapshots.entities.Snapshot

class AddFragment : Fragment() {

    private lateinit var mBinding: FragmentAddBinding
    private lateinit var mStorageReference: StorageReference
    private lateinit var mDatabaseReference: DatabaseReference
    private var mPhotoSelectedUri: Uri? = null

    private val addFragmentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            mPhotoSelectedUri = it.data?.data
            mBinding.imgPhoto.setImageURI(mPhotoSelectedUri)
            mBinding.txtIlTitle.visibility = View.VISIBLE
            mBinding.txtViewTitle.text = getString(R.string.post_message_valid_title)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        mBinding = FragmentAddBinding.inflate( inflater , container ,false )
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.btnPost.setOnClickListener{ postSnapshot() }
        mBinding.btnImg.setOnClickListener{ openGallery() }
        mStorageReference = FirebaseStorage.getInstance().reference
        mDatabaseReference = FirebaseDatabase.getInstance().reference.child(SnapshotsApplication.PATH_SNAPSHOTS)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        addFragmentLauncher.launch(intent)
    }

    private fun postSnapshot() {
        mBinding.progressBar.visibility = View.VISIBLE
        val key = mDatabaseReference.push().key!!
        val storageReference = mStorageReference.child(SnapshotsApplication.PATH_SNAPSHOTS)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(key)
        if (mPhotoSelectedUri != null) {
            storageReference.putFile(mPhotoSelectedUri!!)
                .addOnProgressListener {
                    val progress = ( 100 * it.bytesTransferred / it.totalByteCount ).toDouble()
                    mBinding.txtViewTitle.text = "$progress"
                }
                .addOnCompleteListener {
                    mBinding.progressBar.visibility = View.INVISIBLE
                }
                .addOnSuccessListener {
                    Snackbar.make(mBinding.root, "Salio todo correcto rey", Snackbar.LENGTH_SHORT).show()
                    it.storage.downloadUrl.addOnSuccessListener {
                        saveSnapshot(key, it.toString(), mBinding.txtEditTitle.text.toString().trim())
                        mBinding.txtIlTitle.visibility = View.GONE
                        mBinding.txtViewTitle.text = getString(R.string.post_message_title)
                    }
                }
                .addOnFailureListener {
                    Snackbar.make(mBinding.root, "Salio todo mal rey", Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveSnapshot (key: String, url: String, title: String) {
        val snapshot = Snapshot(title = title, photoUrl = url)
        mDatabaseReference.child(key).setValue(snapshot)
    }

}