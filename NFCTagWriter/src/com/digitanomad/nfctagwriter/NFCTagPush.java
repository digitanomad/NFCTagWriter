package com.digitanomad.nfctagwriter;

import java.util.List;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.widget.TextView;

public class NFCTagPush extends Activity {

	private NfcAdapter mAdapter;
	private NdefMessage mMessage;

	private TextView mText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfctag_push);

		mText = (TextView) findViewById(R.id.text);

		mAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mAdapter != null) {
			mMessage = (NdefMessage) getIntent().getExtras().get("tag");
			mText.setText("푸쉬할 메시지\n " + getTagMessage(mMessage)
					+ "\n\n 다른 안드로이드 폰의 NFC 앱을 터치하세요");

			mAdapter.setNdefPushMessage(mMessage, this);
			mAdapter.setOnNdefPushCompleteCallback(
					new OnNdefPushCompleteCallback() {

						@Override
						public void onNdefPushComplete(NfcEvent event) {
							mText.setText("전송완료");
						}
					}, this);

		} else {
			mText.setText("먼저 NFC를 활성화하세요.");
		}
	}

	private String getTagMessage(NdefMessage mMessage) {
		List<ParsedRecord> records = NdefMessageParser.parse(mMessage);
		final int size = records.size();
		StringBuffer resultStr = new StringBuffer();

		for (int i = 0; i < size; i++) {
			ParsedRecord record = records.get(i);

			int recordType = record.getType();
			String recordStr = "";
			if (recordType == ParsedRecord.TYPE_TEXT) {
				recordStr = "TEXT: " + ((TextRecord) record).getText() + "\n";
			} else if (recordType == ParsedRecord.TYPE_URI) {
				recordStr = "URI: " + ((UriRecord) record).getUri().toString()
						+ "\n";
			}

			resultStr.append(recordStr);
		}

		return resultStr.toString();
	}

}
