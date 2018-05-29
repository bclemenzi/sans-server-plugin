package com.nfbsoftware.sansserverplugin.sdk.util;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Generic string utility class to help deal with common String events.
 * 
 * @author Brendan Clemenzi
 */
public class StringUtil
{
   /**
    * An empty string that is only constructed once for performance.
    */
   public final static String EMPTY_STRING = "";
   
   /**
    * table to convert a nibble to a hex character
    */
   public static char[] hexChar = {
        '0' , '1' , '2' , '3' ,
        '4' , '5' , '6' , '7' ,
        '8' , '9' , 'A' , 'B' ,
        'C' , 'D' , 'E' , 'F' };
   
   /**
    * 
    * @param s
    * @return
    */
   public static String emptyIfNull(String s)
   {
       return s != null ? s : EMPTY_STRING;
   }
   
   /**
    * 
    * @param s
    * @param defaultValue
    * @return
    */
   public static String replaceIfNull(String s, String defaultValue)
   {
       return (!isNullOrEmpty(s)) ? s : defaultValue;
   }

   /**
    * 
    * @param s
    * @return
    */
   public static String nullIfEmpty(String s)
   {
       if (s != null)
       {
           // s = s.trim();
           if (s.length() == 0)
           {
               s = null;
           }
       }

       return s;
   }

   /**
    * 
    * @param s
    * @return
    */
   public static String nullIfEmptyTrim(String s)
   {
       if (s != null)
       {
           s = s.trim();
           if (s.length() == 0)
           {
               s = null;
           }
       }

       return s;
   }

   /**
    * 
    * @param s
    * @return
    */
   public static boolean isNullOrEmpty(String s)
   {
       if ((s == null) || (s.length() == 0))
           return true;
       
       return false;
   }

   /**
    * 
    * @param s
    * @return
    */
   public static boolean isNullOrEmptyTrim(String s)
   {
       if (s == null)
           return true;

       s = s.trim();
       if (s.length() == 0)
           return true;

       return false;
   }
   
    /**
     * 
     * @param str
     * @param oldsubstr
     * @param newsubstr
     * @return
     */
    public static String replaceSubstr(String str, String oldsubstr, String newsubstr)
    {
        int substrPos = 0;
        int startPos = 0;
        StringBuffer strBuf = new StringBuffer();

        while ((substrPos = str.indexOf(oldsubstr, startPos)) > -1)
        {
            strBuf.append(str.substring(startPos, substrPos));
            strBuf.append(newsubstr);
            startPos = substrPos + oldsubstr.length();
        }

        strBuf.append(str.substring(startPos));
        return strBuf.toString();
    }
    
    /**
     * 
     * @param s
     * @param sToken
     * @return
     */
    public static String[] split(String s, String sToken)
    {
        int iTokenLength=sToken.length();
        if (sToken == null || (iTokenLength) == 0){
            return new String[] {s};
        }

        int iCount=0;
        int iBegin=0;
        int iEnd;

        while((iEnd = s.indexOf(sToken, iBegin)) != -1) {
            iCount++;
            iBegin = iEnd + iTokenLength;
        }
        iCount++;

        // allocate an array to return the tokens,
        // we now know how big it should be
        String[] result = new String[iCount];

        // Scan s again, but this time pick out the tokens
        iCount = 0;
        iBegin = 0;
        while((iEnd = s.indexOf(sToken, iBegin)) != -1) {
            result[iCount] = (s.substring(iBegin, iEnd));
            iCount++;
            iBegin = iEnd + iTokenLength;
        }
        iEnd = s.length();
        result[iCount] = s.substring(iBegin, iEnd);

        return (result);
    }
    
    /**
     * 
     * @param s
     * @return
     */
    public static String replaceJavaLiteral(String s){
        int length = s.length();
        int newLength = length;
        // first check for characters that migh
        // be dangerous and calculate a length
        // of the string that has escapes.
        for (int i=0; i<length; i++){
            char c = s.charAt(i);
            switch(c){
                case '\"':
                case '\'':
                case '\n':
                case '\r':
                case '\t':
                case '\\':{
                    newLength += 1;
                } break;
            }
        }
        if (length == newLength){
            // nothing to escape in the string
            return s;
        }
        StringBuffer sb = new StringBuffer(newLength);
        for (int i=0; i<length; i++){
            char c = s.charAt(i);
            switch(c){
                case '\"':{
                    sb.append("\\\"");
                } break;
                case '\'':{
                    sb.append("\\\'");
                } break;
                case '\n':{
                    sb.append("\\n");
                } break;
                case '\r':{
                    sb.append("\\r");
                } break;
                case '\t':{
                    sb.append("\\t");
                } break;
                case '\\':{
                    sb.append("\\\\");
                } break;
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }
    
    /**
     * 
     * @param value
     * @param other
     * @return
     */
    public static String OnEmptyUseOther(String value, String other)
    {
        //Is it empty?
        if (isNullOrEmptyTrim(value))
        {
            return other;
        }
        
        return value.trim();
    }
    
    /**
     * 
     * @param stringValue
     * @param paddingChar
     * @param paddingLength
     * @return
     */
    public static String stringPaddingBefore(String stringValue, String paddingChar, int paddingLength)
    {
        StringBuffer buffer = new StringBuffer(128);
        
        for (int i = 0, length = stringValue.length(); i < (paddingLength - length); i++)
        {
            buffer.append(paddingChar);
        }
        
        buffer.append(stringValue);
        
        return buffer.toString();
    }
    
    /**
     * 
     * @param stringValue
     * @param paddingChar
     * @param paddingLength
     * @return
     */
    public static String stringPaddingAfter(String stringValue, String paddingChar, int paddingLength)
    {
        StringBuffer buffer = new StringBuffer(128);
        buffer.append(stringValue);
        
        for (int i = 0, length = stringValue.length(); i < (paddingLength - length); i++)
        {
            buffer.append(paddingChar);
        }
        
        return buffer.toString();
    }
    
    /**
     * 
     * @param tokenString
     * @param separators
     * @return
     */
    public static String[] parseString(String tokenString, String separators)
    {
        // Get the character array from the string for fast index lookups.
        char[] tokenStringChars = tokenString.toCharArray();
        int len = tokenStringChars.length;
        // Guess at the initial capacity of the array to be one third of
        // the total length of the input string.
        ArrayList tokens = new ArrayList((len/3)+1);

        // If there are no separators, then just return the input string.
        if ((separators == null) || (separators.length() == 0))
        {
            // Add entire input string as one token unless it is empty.
            if (tokenString.length() > 0)
            {
                tokens.add(tokenString);
            }
            String[] tokenArray = new String[tokens.size()];
            tokens.toArray(tokenArray);
            return tokenArray;
        }
        
        // Speed optimization if there is only a single separator.
        if (separators.length() == 1)
        {
            int i1=0, i2;
            char separatorChar = separators.charAt(0);

            // Strip leading separators
            while ((i1 < len) && (tokenStringChars[i1] == separatorChar))
                i1++;

            while (i1 < len)
            {
                i2 = tokenString.indexOf(separatorChar, i1);
                if (i2 < 0)
                {
                    tokens.add(tokenString.substring(i1));
                    break;
                }
                
                tokens.add(tokenString.substring(i1, i2));

                i1 = i2 + 1;

                // Strip consecutive separators
                while ((i1 < len) && (tokenStringChars[i1] == separatorChar))
                    i1++;
            }
        }
        else // Multiple separators
        {
            int i1=0, i2;
            
            // Strip leading separators
            while ((i1 < len) &&
                   (separators.indexOf(tokenStringChars[i1]) >= 0))
                i1++;
            
            while (i1 < len)
            {
                i2 = i1;
                
                while ((i2 < len) &&
                       (separators.indexOf(tokenStringChars[i2]) < 0))
                {
                    i2++;
                }
                
                tokens.add(tokenString.substring(i1, i2));
                
                i1 = i2 + 1;

                // Strip consecutive separators
                while ((i1 < len) &&
                       (separators.indexOf(tokenStringChars[i1]) >= 0))
                    i1++;
            }
        }
        
        String[] tokenArray = new String[tokens.size()];
        if (tokens.size() > 0)
            tokens.toArray(tokenArray);
        
        return tokenArray;
    }

    /**
     * 
     * @param dollars
     * @return
     */
    public static String convertToDollars(double dollars)
    {
        // Convert the double to a currency value
        NumberFormat ncf = NumberFormat.getCurrencyInstance(Locale.US); 
        String dollarString = ncf.format(dollars);
        
        // Remove commas and dollar symbols
        dollarString = replaceSubstr(dollarString, "$", "");
        dollarString = replaceSubstr(dollarString, ",", "");
        
        return dollarString;
    }
    
    /**
     * 
     * @param b
     * @return
     */
    public static String toHexString( byte[] b )
    {
        StringBuffer sb = new StringBuffer( b.length * 2 );
        
        for ( int i=0 ; i<b.length ; i++ )
        {
            // look up high nibble char
            sb.append( hexChar [ ( b[ i] & 0xf0 ) >>> 4 ] ) ;

            // look up low nibble char
            sb.append( hexChar [ b[ i] & 0x0f ] ) ;
        }
        
        return sb.toString();
    }
    
    /**
     * 
     * @param dateField
     * @return
     */
    public static Date convertToDate(String dateField)
    {
        Date issueDate = new Date(new java.util.Date().getTime());
        
        if(!isNullOrEmpty(dateField))
        {
            String[] splitString = StringUtil.split(dateField, "/");
            
            String tempMonth = splitString[0];
            String tempDay = splitString[1];
            String tempYear = splitString[2];
            
            String dayString = (tempDay.length() == 1) ? "0" + tempDay : tempDay;
            String monthString = (tempMonth.length() == 1) ? "0" + tempMonth : tempMonth;
            String yearString = (tempYear.length() == 2) ? "20" + tempYear : tempYear;
            
            int day = new Integer(dayString).intValue();
            int month = new Integer(monthString).intValue();
            int year = new Integer(yearString).intValue();
            
            // Setup Calendar Object
            Calendar Cal = Calendar.getInstance();
            Cal.set(year,(month - 1),day);
            
            // Create java.sql.Date object
            issueDate = new Date(Cal.getTime().getTime());
        }
        
        return issueDate;
    }
    
    /**
     * 
     * @param dateField
     * @return
     */
    public static Date convertToMMDDYYYDate(String dateField)
    {
        Date issueDate = new Date(new java.util.Date().getTime());
        
        if(!isNullOrEmpty(dateField))
        {
            int day = new Integer(dateField.substring(3, 5)).intValue();
            int month = new Integer(dateField.substring(0, 2)).intValue();
            int year = new Integer(dateField.substring(6, 10)).intValue();
            
            // Setup Calendar Object
            Calendar Cal = Calendar.getInstance();
            Cal.set(year,(month - 1),day);
            
            // Create java.sql.Date object
            issueDate = new Date(Cal.getTime().getTime());
        }
        
        return issueDate;
    }
    
    /**
     * 
     * @param dateField
     * @param timeField
     * @return
     */
    public static Calendar convertToCalendar(String dateField, String timeField)
    {
        Calendar issueDate = Calendar.getInstance();
        
        if(!isNullOrEmpty(dateField))
        {
            String[] dateArray = dateField.split("/");
            String[] timeArray = timeField.split(":");
            
            int day = Integer.parseInt(dateArray[1]);
            int month = Integer.parseInt(dateArray[0]);
            int year = Integer.parseInt(dateArray[2]);
            
            int hour = Integer.parseInt(timeArray[0]);
            int minute = Integer.parseInt(timeArray[1]);
            
            // Setup Calendar Object
            Calendar Cal = Calendar.getInstance();
            issueDate.set(year,(month - 1),day);
            issueDate.set(Cal.HOUR_OF_DAY, hour);
            issueDate.set(Cal.MINUTE, minute);
            issueDate.set(Cal.SECOND, 0);
            issueDate.set(Cal.MILLISECOND, 0);
        }
        
        return issueDate;
    }
    
    /**
     * 
     * @param dateField
     * @return
     */
    public static Date convertToDateMorning(String dateField)
    {
        Date issueDate = new Date(new java.util.Date().getTime());
        
        if(!isNullOrEmpty(dateField))
        {
            String[] dateArray = dateField.split("/");
            
            int day = Integer.parseInt(dateArray[1]);
            int month = Integer.parseInt(dateArray[0]);
            int year = Integer.parseInt(dateArray[2]);
            
            // Setup Calendar Object
            Calendar Cal = Calendar.getInstance();
            Cal.set(year,(month - 1),day);
            Cal.set(Cal.HOUR_OF_DAY, 0);
            Cal.set(Cal.MINUTE, 0);
            Cal.set(Cal.SECOND, 0);
            Cal.set(Cal.MILLISECOND, 0);

            
            // Create java.sql.Date object
            issueDate = new Date(Cal.getTime().getTime());
        }
        
        return issueDate;
    }
    
    /**
     * 
     * @param dateField
     * @return
     */
    public static Calendar convertToCalendarDateMorning(String dateField)
    {
        Calendar issueDate = Calendar.getInstance();
        
        if(!isNullOrEmpty(dateField))
        {
            String[] dateArray = dateField.split("/");
            
            int day = Integer.parseInt(dateArray[1]);
            int month = Integer.parseInt(dateArray[0]);
            int year = Integer.parseInt(dateArray[2]);
            
            // Setup Calendar Object
            issueDate.set(year,(month - 1),day);
            issueDate.set(Calendar.HOUR_OF_DAY, 0);
            issueDate.set(Calendar.MINUTE, 0);
            issueDate.set(Calendar.SECOND, 0);
            issueDate.set(Calendar.MILLISECOND, 0);
        }
        
        return issueDate;
    }
    
    /**
     * 
     * @param dateField
     * @return
     */
    public static Date convertToDateMorning(Date dateField)
    {
        Date tempDate = new Date(new java.util.Date().getTime());
        
        // Setup Calendar Object
        Calendar Cal = Calendar.getInstance();
        Cal.setTime(dateField);
        Cal.set(Calendar.HOUR_OF_DAY, 0);
        Cal.set(Calendar.MINUTE, 0);
        Cal.set(Calendar.SECOND, 0);
        Cal.set(Calendar.MILLISECOND, 0);

        
        // Create java.sql.Date object
        tempDate = new Date(Cal.getTimeInMillis());
        
        return tempDate;
    }
    
    /**
     * 
     * @param dateField
     * @return
     */
    public static Date convertToDateMidnight(String dateField)
    {
        Date issueDate = new Date(new java.util.Date().getTime());
        
        if(!isNullOrEmpty(dateField))
        {
            String[] dateArray = dateField.split("/");
            
            int day = Integer.parseInt(dateArray[1]);
            int month = Integer.parseInt(dateArray[0]);
            int year = Integer.parseInt(dateArray[2]);
            
            // Setup Calendar Object
            Calendar Cal = Calendar.getInstance();
            Cal.set(year,(month - 1),day);
            Cal.set(Cal.HOUR_OF_DAY, 23);
            Cal.set(Cal.MINUTE, 59);
            Cal.set(Cal.SECOND, 59);
            Cal.set(Cal.MILLISECOND, 0);

            
            // Create java.sql.Date object
            issueDate = new Date(Cal.getTime().getTime());
        }
        
        return issueDate;
    }
    
    /**
     * 
     * @param dateField
     * @return
     */
    public static Calendar convertToCalendarDateMidnight(String dateField)
    {
        Calendar issueDate = Calendar.getInstance();
        
        if(!isNullOrEmpty(dateField))
        {
            String[] dateArray = dateField.split("/");
            
            int day = Integer.parseInt(dateArray[1]);
            int month = Integer.parseInt(dateArray[0]);
            int year = Integer.parseInt(dateArray[2]);
            
            // Setup Calendar Object
            issueDate.set(year,(month - 1),day);
            issueDate.set(Calendar.HOUR_OF_DAY, 23);
            issueDate.set(Calendar.MINUTE, 59);
            issueDate.set(Calendar.SECOND, 59);
            issueDate.set(Calendar.MILLISECOND, 0);
        }
        
        return issueDate;
    }
    
    /**
     * 
     * @param dateField
     * @return
     */
    public static Date convertToDateMidnight(Date dateField)
    {
        Date tempDate = new Date(new java.util.Date().getTime());
        
        // Setup Calendar Object
        Calendar Cal = Calendar.getInstance();
        Cal.setTime(dateField);
        Cal.set(Calendar.HOUR_OF_DAY, 23);
        Cal.set(Calendar.MINUTE, 59);
        Cal.set(Calendar.SECOND, 59);
        Cal.set(Calendar.MILLISECOND, 0);

        // Create java.sql.Date object
        tempDate = new Date(Cal.getTimeInMillis());
        
        return tempDate;
    }
    
    /**
     * 
     * @param inputString
     * @return
     */
    public static String stringPreview(String inputString)
    {
        String tmpString = "";
        
        if(inputString.length() >= 20)
        {
            tmpString = inputString.substring(0, 19);
        }
        else
        {
            tmpString = inputString.substring(0, inputString.length());
        }
        
        return tmpString;
    }
    
    /**
     * 
     * @param s
     * @param defaultValue
     * @return
     */
    public static String replaceIfNullOrError(String s, String defaultValue)
    {
        String tmpString = EMPTY_STRING;
        
        try
        {
            tmpString = replaceIfNull(s, defaultValue);
        }
        catch(Exception e)
        {
            tmpString = defaultValue;
        }
        
        return tmpString;
    }
    
    /**
     * 
     * @param html
     * @return
     */
    public static String stripHTML (String html)
    {
        if (html == null || html.isEmpty()) 
        { 
            return ""; 
        }
        
        if (html.toLowerCase().contains("<body") && html.toLowerCase().contains("</body>")) 
        {
            int beginIndex = html.toLowerCase().indexOf("<body");
            int endIndex = html.toLowerCase().indexOf("</body>");
            endIndex += "</body>".length();
            html = html.substring(beginIndex, endIndex);
        }
        
        for (String tag : new String[]{"style", "script"}) 
        {
            int tries = 10;
            while (tries-- > 0 && html.toLowerCase().contains("<" + tag)) 
            {
                html = removeHTMLBlock(html, tag);
            }
        }
        
        html = html.replaceAll("&#8226;", "");
        html = html.replaceAll("&bull;", "");
        
        // OK.  I should have "simple" markup left.  
        // Since I do not care about HREFs or IMG sources here, 
        // I can be draconian in my clensing
        html = html.replaceAll("</?\\w+[^><]*>", " ");

        return html;        
    }
    
    /**
     * 
     * @param html
     * @param tag
     * @return
     */
    public static String removeHTMLBlock(String html, String tag)
    {
        if (html.toLowerCase().contains("<" + tag) && html.toLowerCase().contains("</" + tag)) 
        {
            int beginIndex = html.toLowerCase().indexOf("<" + tag);
            int endIndex = html.toLowerCase().indexOf("</" + tag);
            endIndex += ("</" + tag + ">").length();
            html = html.substring(0, beginIndex) + html.substring(endIndex);
        }
        
        return html;
    }
    
    /**
     * 
     * @param fullString
     * @param maxLength
     * @return
     */
    public static String truncate(String fullString, int maxLength)
    {
        String tempString = fullString.substring(0, Math.min(fullString.length(), maxLength));
        
        return tempString;
    }
    
    /**
     * 
     * @param s
     * @return
     */
    public static String cleanForXHTMLEntityEncoding(String s)
    {
        s = replaceSubstr(s, "&nbsp;", " ");
        
        return s;
    }
    
    /**
     * 
     * @param s
     * @return
     */
    public static String removeSpecialChar(String s)
    {
        return s != null ? s.replaceAll("[^A-Za-z0-9_ ]", "") : EMPTY_STRING;
    }
    
    /**
     * 
     * @param dirtyString
     * @return
     */
    public static String removeCustomTags(String dirtyString)
    {
        return dirtyString.replaceAll("<\\w+:\\w+>|</\\w+:\\w+>", "");
    }
}
