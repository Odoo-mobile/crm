package com.odoo.addons.crm.receivers;

import java.io.File;
import java.io.IOException;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.odoo.util.logger.OLog;

public class CRMCallRecorder {
	MediaRecorder mRecorder = null;
	String mAudioPath;
	String mFileName = "";
	public static Boolean recordingStarted = false;
	boolean meadiaPathSet = false;

	public void createFile(String contactNumber) {
		if (!isRecording() && contactNumber != null) {
			OLog.log("Contact Number : ",
					contactNumber.replaceAll("[-+.^:, ]", ""));
			mFileName = "PH" + contactNumber.replaceAll("[-+.^:, ]", "") + "_"
					+ System.currentTimeMillis() + ".amr";
			OLog.log("file: " + mFileName);
			generatePath();
		}
	}

	public boolean isRecording() {
		return recordingStarted;
	}

	private void generatePath() {
		String app_path = getAppDirectoryPath() + "/CallAudio";
		File fileDir = new File(app_path);
		if (!fileDir.isDirectory()) {
			fileDir.mkdir();
		}
		mAudioPath = app_path + "/" + mFileName;

	}

	private String getAppDirectoryPath() {
		File externalStorage = Environment.getExternalStorageDirectory();
		String basePath = externalStorage.getAbsolutePath() + "/Odoo";
		File baseDir = new File(basePath);
		if (!baseDir.isDirectory()) {
			baseDir.mkdir();
		}
		return basePath;
	}

	public void startRecording() throws IOException {
		Log.v("CRMCallRecorder", "startRecording()");
		if (recordingStarted) {
			return;
		}
		String state = Environment.getExternalStorageState();
		if (!state.equals(Environment.MEDIA_MOUNTED)) {
			throw new IOException("SD Card is not mounted.  It is " + state
					+ ".");
		}
		// make sure the directory we plan to store the recording in exists
		File directory = new File(mAudioPath).getParentFile();
		if (!directory.exists() && !directory.mkdirs()) {
			throw new IOException("Path to file could not be created.");
		}
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mRecorder.setOutputFile(mAudioPath);
		recordingStarted = true;
		mRecorder.prepare();
		mRecorder.start();
		Log.v("CRMCallRecorder", "Recording started");
	}

	public String stopRecording() {
		if (isRecording()) {
			try {
				mRecorder.stop();
				mRecorder.release();
				mFileName = null;
				Log.v("CRMCallRecorder", "Recording stoped");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		recordingStarted = false;
		return mAudioPath;
	}
}
