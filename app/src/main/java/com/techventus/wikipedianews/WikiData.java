package com.techventus.wikipedianews;

/**
 * Created by josephmalone on 16-08-23.
 */
public class WikiData
{
	public enum DataType
	{
		HEADER,POST;
	}

	DataType mType;
	String mText;

	public WikiData(String text, DataType dataType)
	{
		mText = text;
		mType = dataType;
	}

	public DataType getType()
	{
		return mType;
	}

	public void setType(DataType type)
	{
		this.mType = type;
	}

	public String getText()
	{
		return mText;
	}

	public void setText(String text)
	{
		this.mText = text;
	}
}
