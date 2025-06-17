package com.techventus.wikipedianews.data;

import com.techventus.wikipedianews.WikiData;
import java.util.ArrayList;

public class HardcodedNewsData {

    public static ArrayList<WikiData> getHardcodedData() {
        ArrayList<WikiData> data = new ArrayList<>();

        // Manually curated sample data
        data.add(new WikiData("Topics in the News", WikiData.DataType.HEADER));
        data.add(new WikiData("Ongoing international summit discusses climate change and global trade policies.", WikiData.DataType.POST));
        data.add(new WikiData("Technological advancements in AI lead to new breakthroughs in medical diagnostics.", WikiData.DataType.POST));

        data.add(new WikiData("Ongoing Events", WikiData.DataType.HEADER));
        data.add(new WikiData("The 'Festival of Lights' continues in several cities, attracting large crowds.", WikiData.DataType.POST));
        data.add(new WikiData("Major sports championship enters its final week, with several key matches scheduled.", WikiData.DataType.POST));

        data.add(new WikiData("Recent Deaths", WikiData.DataType.HEADER));
        data.add(new WikiData("Renowned artist and philanthropist passes away at age 85.", WikiData.DataType.POST));

        data.add(new WikiData("Today's Highlights - July 27, 2024", WikiData.DataType.HEADER));
        data.add(new WikiData("Stock markets show mixed results in morning trading.", WikiData.DataType.POST));
        data.add(new WikiData("Expected heatwave prompts public health warnings in several regions.", WikiData.DataType.POST));

        return data;
    }
}
