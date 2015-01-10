package com.digitanomad.nfctagwriter;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import android.nfc.NdefRecord;

import com.google.common.base.Preconditions;

public class TextRecord implements ParsedRecord {

	/** ISO/IANA language code */
	private final String mLanguageCode;
	
	private final String mText;
	
	private TextRecord(String languageCode, String text) {
		mLanguageCode = Preconditions.checkNotNull(languageCode);
		mText = Preconditions.checkNotNull(text);
	}
	
	@Override
	public int getType() {
		return ParsedRecord.TYPE_TEXT;
	}
	
	public String getText() {
		return mText;
	}
	
	/**
	 * �ؽ�Ʈ�� ������ ISO/IANA language code�� ��ȯ
	 */
	public String getLanguageCode() {
		return mLanguageCode;
	}
	
	/**
	 * {@link android.nfc.NdefRecord}�� �м��ؼ� ����ڵ�� �ؽ�Ʈ�� ��� {@link TextRecord}�� ��ȯ�Ѵ�.
	 * @param record
	 * @return {@link TextRecord}
	 */
	public static TextRecord parse(NdefRecord record) {
		Preconditions.checkArgument(record.getTnf() == NdefRecord.TNF_WELL_KNOWN);
		Preconditions.checkArgument(Arrays.equals(record.getType(), NdefRecord.RTD_TEXT));
		try {
			byte[] payload = record.getPayload();
			/*
			 * NFC Forum "Text Record Type Definition" section 3.2.1.
			 * payload[0]���� "Status Byte Encodings(���ڵ� ����)" �ʵ尡 ����ִ�.
			 * 
			 * bit7�� �ؽ�Ʈ ���ڵ� �ʵ��.
			 *  if (Bit_7 == 0): UTF-8�� ���ڵ��� �ؽ�Ʈ
			 *  if (Bit_7 == 1): UTF-16���� ���ڵ��� �ؽ�Ʈ
			 *  
			 *  Bit6�� �̷�������ϱ� ���� ���ܵ� ��Ʈ�̹Ƿ� 0���� ����.
			 *  Bit 0���� 5�� IANA language code�� ����
			 */
			String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
			int languageCodeLength = payload[0] & 0077;
			String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
			String text = new String(payload, languageCodeLength + 1, 
					payload.length - languageCodeLength - 1, textEncoding);
			
			return new TextRecord(languageCode, text);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static boolean isText(NdefRecord record) {
		try {
			parse(record);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

}
