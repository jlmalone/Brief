package com.techventus.wikipedianews;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.squareup.okhttp.OkHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


//		StringBuilder buf = new StringBuilder();
//
//		BufferedReader
//		in = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.sample)),65536);
//		String str;
//
//		try
//		{
//			while ((str = in.readLine()) != null)
//			{
//				buf.append(str);
//			}
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//		String sample = buf.toString();
//
//		//Everything after In the News
//		String slice1 = sample.substring(sample.indexOf("In the News"));
//		String todayraw = slice1.substring(0,slice1.indexOf("</table>"));
//
//		//Break into LI Array
//		List<String> lisArrayList = new ArrayList<>();
//		String[] lis = todayraw.split("<li>");
//		for(int i=1;i<lis.length;i++)
//		{
//			String html = lis[i].substring(0,lis[i].indexOf("</li>"));
//			lisArrayList.add(html);
//		}

	}
}
