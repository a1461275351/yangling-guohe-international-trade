package cn.nodesoft.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 时间工具类
 * 
 * @author ruoyi
 */
public class DateUtil extends org.apache.commons.lang3.time.DateUtils
{
    public static String YYYY = "yyyy";
    
    public static String DD = "dd";

    public static String YYYY_MM = "yyyy-MM";

    public static String YYYY_MM_DD = "yyyy-MM-dd";

    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private static String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM", 
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};
    private static long res;
    
    /**
     * 获取当前Date型日期
     * 
     * @return Date() 当前日期
     */
    public static Date getNowDate() {
        return new Date();
    }

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     * 
     * @return String
     */
    public static String getDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    public static final String getTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    public static final String getMM() { return dateTimeNow(YYYY_MM);
    }
    
    
    public static final String getDD() {
        return dateTimeNow(DD);
    }
    
    public static final String dateTimeNow() {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    public static final String dateTimeNow(final String format) {
        return parseDateToStr(format, new Date());
    }

    public static final String dateTime(final Date date) {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    public static final String parseDateToStr(final String format, final Date date) {
        return new SimpleDateFormat(format).format(date);
    }

    public static final Date dateTime(final String format, final String ts) {
        try {
            return new SimpleDateFormat(format).parse(ts);
        }catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static final String datePath() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static final String dateTime() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    /**
     * 日期型字符串转化为日期 格式
     */
    public static Date parseDate(Object str) {
        if (str == null)
        {
            return null;
        }
        try
        {
            return parseDate(str.toString(), parsePatterns);
        }
        catch (ParseException e)
        {
            return null;
        }
    }
    
    /**
     * 获取服务器启动时间
     */
    public static Date getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     * 计算两个时间差
     */
    public static String getDatePoor(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        long sec = diff % nd % nh % nm / ns;
        if (day > 0 ) {
            return day + "天" + hour + "小时" + min + "分钟" + sec + "秒";
        } else if (day == 0 && hour > 0) {
            return hour + "小时" + min + "分钟" + sec + "秒";
        } else if (day == 0 && hour == 0 && min > 0) {
            return min + "分钟" + sec + "秒";
        } else if (day == 0 && hour == 0 && min == 0 && sec > 0) {
            return sec + "秒";
        } else {
            return "0秒";
        }
    }
    
    /**
     * 
     * @Title: getSubStrType
     * @Description: 计算时间差距
     * @param @param beginDateStr
     * @param @param endDateStr
     * @param @return
     * @param @throws java.text.ParseException 参数
     * @return long 返回类型
     * @throws
     */
    public static Map getSubStrType(String beginDateStr,String endDateStr) throws ParseException {
    	Map resmap=new HashMap();
       long nd = 1000 * 24 * 60 * 60;
       long nh = 1000 * 60 * 60;
       long nm = 1000 * 60;
       SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       Date beginDate;
       Date endDate;
       beginDate = format.parse(beginDateStr);
       endDate= format.parse(endDateStr);
       // 获得两个时间的毫秒时间差异
       long diff = beginDate.getTime() - endDate.getTime();
       // 计算差多少天
       long day = diff / nd;
       // 计算差多少小时
       long hour = diff % nd / nh;
       // 计算差多少分钟
       long min = diff % nd % nh / nm;
       resmap.put("day","0");
       resmap.put("hour","0");
       resmap.put("min","0");
       if(Math.abs(day)>0) {
    	   //大于1天
    	   res=Math.abs(day);
    	   resmap.put("day", Math.abs(day));
       }else {
    	   if( Math.abs(hour)>0) {
    		   //大于1小时
    		   res=Math.abs(hour);
    		   resmap.put("hour", Math.abs(hour));
    	   }else {
    		   if(Math.abs(min)>3) {
    			   res=Math.abs(min)+1;
    			   resmap.put("min", Math.abs(min)+1);
    		   }
    	   }
       }
       return resmap;
    }

    /**
     * 获取当前日期是星期几
     * @param dt
     * @return 当前日期是星期几
     */
    public static String getWeekOfDate(Date dt) {
        String[] weekDays = {"星期天", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);

        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }



    /**
     *	日期转换为String
     * @param date
     * @return
     */
    public static String dateOfStr(Date date) {
    	SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.YYYY_MM_DD);
    	return sdf.format(date);
    }

    public static String dateOfStrs(Date date) {
    	SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.YYYY_MM_DD_HH_MM_SS);
    	return sdf.format(date);
    }

    /**
     *
     * @Title: getDaysOfMonth
     * @Description: 某个月一共有多少天
     * @param @param date
     * @param @return 参数
     * @return int 返回类型
     * @throws
     */
    public static int getDaysOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * @Title: frontDate
	 * @Description: 获取当前时间的前后时间节点
	 * @param  date 当前时间
	 * @param  num 前期num(以前为负数，以后为正数)
	 * @param  type 前期的类型(年 year,月 mon,日 day)
	 *  @param  formats 时间 格式。默认yyyy-MM-dd HH:mm:ss
	 * @return String 返回类型
	 * @throws
	 */
    public static String frontDate(String date, Integer num, String type,String formats) {
    	String frontDate = "";
    	if(StringUtils.isBlank(formats)) {
    		formats = DateUtil.YYYY_MM_DD_HH_MM_SS;
    	}

    	SimpleDateFormat format = new SimpleDateFormat(formats);
    	try {
			Date dateTime = format.parse(date);
			Calendar c = Calendar.getInstance();
	        if ("year".equals(type)) {
	        	c.setTime(dateTime);
	            c.add(Calendar.YEAR, num);
	            Date y = c.getTime();
	            frontDate = format.format(y);
			}else if ("mon".equals(type)) {
				c.setTime(dateTime);
		        c.add(Calendar.MONTH, num);
		        Date m = c.getTime();
		        frontDate = format.format(m);
			}else {
				c.setTime(dateTime);
		        c.add(Calendar.DATE, num);
		        Date d = c.getTime();
		        frontDate = format.format(d);
			}
	        return frontDate;
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	return null;
    }

    /**
     * 获取一年中所有的周末
     * 注意 TODO：此接口不能按照国家标准去除节假日补班的情况，如果某一个周六或周日实际为节假日补班，也会加入到结果集里
     * @param year
     * @return
     */
    public static List<String> listWeekendByYear(String year) {
        Set<String> list = new HashSet<>();
        if (org.apache.commons.lang3.StringUtils.isEmpty(year)) {
            return new ArrayList<>(list);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.parseInt(year), 0, 1);
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Integer.parseInt(year) + 1, 0, 1);
        while (calendar.compareTo(calendar1) < 0) {
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                // 只查询当年的
                if (year.equals(calendar.get(Calendar.YEAR)+"")) {
                    String day = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
                    // 对日期进行标准格式化
                    day = dateFormt(day);
                    list.add(day);
                }
            }
            calendar.add(Calendar.DATE,1);
        }
        return new ArrayList<>(list);
    }

    /**
     *	根据当前日期获得是星期几
     * time=yyyy-MM-dd
     * @return
     */
    public static String getWeek(String time) {
        String Week = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(format.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int wek=c.get(Calendar.DAY_OF_WEEK);
        if (wek == 1) {
            Week += "星期日";
        }
        if (wek == 2) {
            Week += "星期一";
        }
        if (wek == 3) {
            Week += "星期二";
        }
        if (wek == 4) {
            Week += "星期三";
        }
        if (wek == 5) {
            Week += "星期四";
        }
        if (wek == 6) {
            Week += "星期五";
        }
        if (wek == 7) {
            Week += "星期六";
        }
        return Week;
    }

    public static Boolean isWorkDay(String date) {
    	return !getWeek(date).equals("星期六") && !getWeek(date).equals("星期日");
    }


    /**
     *	两个时间之间相差多少小时
     * @param starttime 时间参数 1：
     * @param endtime 时间参数 2：
     * @return 相差天数
     */
    public static double getDistanceDays(String starttime, String endtime) throws Exception{
        DateFormat df = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
        Date one;
        Date two;
        double days=0;
        try {
            one = df.parse(starttime);
            two = df.parse(endtime);
            long time1 = one.getTime();
            long time2 = two.getTime();
            long diff ;
            if(time1<time2) {
                diff = time2 - time1;
            } else {
                diff = time1 - time2;
            }

            days = (double)diff / (1000 * 60 * 60 );
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //返回相差多少小时
        return days;
    }


    /**
     * 返回相差多少小时
     * @param starttime 开始时间
     * @param endtime 结束时间
     * @return
     * @throws Exception
     */
	public static double getDistanceHours(String starttime, String endtime) throws Exception{
	    DateFormat df = new SimpleDateFormat("HH:mm:ss");
	    Date one;
	    Date two;
	    double days=0;
	    try {
	        one = df.parse(starttime);
	        two = df.parse(endtime);
	        long time1 = one.getTime();
	        long time2 = two.getTime();
	        long diff ;
	        if(time1<time2) {
	            diff = time2 - time1;
	        } else {
	            diff = time1 - time2;
	        }
	        days = (double)diff / (1000 * 60 * 60 );
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
	  //返回相差多少小时
	    return days;
	}

	/**
     * 返回相差多少小时
     * @param starttime 开始时间
     * @param endtime 结束时间
     * @return
     * @throws Exception
     */
	public static double getDistanceMinute(String starttime, String endtime) throws Exception{
	    DateFormat df = new SimpleDateFormat("HH:mm:ss");
	    Date one;
	    Date two;
	    double days=0;
	    try {
	        one = df.parse(starttime);
	        two = df.parse(endtime);
	        long time1 = one.getTime();
	        long time2 = two.getTime();
	        long diff ;
	        if(time1<time2) {
	            diff = time2 - time1;
	        } else {
	            diff = time1 - time2;
	        }
	        //days = (double)diff / (1000 * 24 * 60 * 60);
	        //days = (double)diff % (1000 * 24 * 60 * 60) % (1000 * 60 * 60)/(1000 * 60) ;
	        days = diff/1000/60;
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
	  //返回相差多少小时
	    return days;
	}


    /**
     *	获取两个日期间年月集合
     * @param minDate 最小时间
     * @param maxDate 最大时间
     * @return 日期集合 格式为 年-月
     * @throws Exception
     */
    public static List<String> getMonthBetween(String minDate, String maxDate) throws Exception {
        ArrayList<String> result = new ArrayList<String>();
        //格式化为年月
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");

        Calendar min = Calendar.getInstance();
        Calendar max = Calendar.getInstance();

        min.setTime(sdf.parse(minDate));
        min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);

        max.setTime(sdf.parse(maxDate));
        max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);

        Calendar curr = min;
        while (curr.before(max)) {
         result.add(sdf.format(curr.getTime()));
         curr.add(Calendar.MONTH, 1);
        }
        return result;
    }

    /**
     *  获取两个日期间年月集合
     * @param minDate 最小时间  2015-01
     * @param maxDate 最大时间 2015-10
     * @return 日期集合 格式为 年-月
     * @throws Exception
     */
    public static List<String> getYearBetween(String minDate, String maxDate) throws Exception {
        ArrayList<String> result = new ArrayList<String>();
        //格式化为年月
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");

        Calendar min = Calendar.getInstance();
        Calendar max = Calendar.getInstance();

        min.setTime(sdf.parse(minDate));
        min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);

        max.setTime(sdf.parse(maxDate));
        max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);

        Calendar curr = min;
        while (curr.before(max)) {
         result.add(sdf.format(curr.getTime()));
         curr.add(Calendar.MONTH, 1);
        }
        return result.stream().distinct().collect(Collectors.toList());
    }


	/**
	 *
	 * @Title: getDays
	 * @Description: 获取两个时间节点之间的所有的时间
	 * @param @param startTime 开始时间
	 * @param @param endTime 结束时间
	 * @param @return 参数
	 * @return List<String> 所有的时间
	 * @throws
	 */
	public static List<String> getDays(String startTime, String endTime) {
        // 返回的日期集合
        List<String> days = new LinkedList<String>();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);

            Calendar tempStart = Calendar.getInstance();
            tempStart.setTime(start);

            Calendar tempEnd = Calendar.getInstance();
            tempEnd.setTime(end);
            tempEnd.add(Calendar.DATE, +1);// 日期加1(包含结束)
            while (tempStart.before(tempEnd)) {
                days.add(dateFormat.format(tempStart.getTime()));
                tempStart.add(Calendar.DAY_OF_YEAR, 1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return days;
    }


	/**
	 *  java 获取 获取某年某月 所有日期（yyyy-mm-dd格式字符串）
	 * @param year
	 * @param month
	 * @return
	 */
	public static List<String> getMonthFullDay(int year , int month){
	    SimpleDateFormat dateFormatYYYYMMDD = new SimpleDateFormat("yyyy-MM-dd");
	    List<String> fullDayList = new ArrayList<>(32);
	    // 获得当前日期对象
	    Calendar cal = Calendar.getInstance();
	    cal.clear();// 清除信息
	    cal.set(Calendar.YEAR, year);
	    // 1月从0开始
	    cal.set(Calendar.MONTH, month-1 );
	    // 当月1号
	    cal.set(Calendar.DAY_OF_MONTH,1);
	    int count = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	    for (int j = 1; j <= count ; j++) {
	    	String time = dateFormatYYYYMMDD.format(cal.getTime());
	    	String str =  time.substring(0,7);
	    	String str2 = year + "-" + (month >= 10 ? month : "0" + month);
	    	if(str.equals(str2)) {
	    		fullDayList.add(dateFormatYYYYMMDD.format(cal.getTime()));
		        cal.add(Calendar.DAY_OF_MONTH,1);
	    	}

	    }
	    return fullDayList;
	}


	public static void main(String[] args) {
//	    String dataDate="2022-10-30";
//        Date nextDay = getPerDay(new Date());
//        String s = dateOfStr(getPerDay(new Date()));
//        System.out.print(s);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        // 上月的最后一天
        calendar.add(Calendar.MONTH, -1);
        Date yesterday = calendar.getTime();
        // 上月的月份
        int lastMonth = DateUtil.getMonth(yesterday);
        System.out.print(lastMonth);
//        System.out.println(s.substring(5,7));
//        System.out.println(s.substring(0,4));
//        Date da=new Date();
//        System.out.print(da.toString());
//        System.out.print(dateFormt("2022-5-31 0:00"));
//        System.out.println(da != null?0:1);
//	    System.out.print(DateUtil.dateTime(DateUtil.YYYY_MM_DD,DateUtil.dateFormt("2022/5/31 0:00")));
//        System.out.print(getLastMonth("2022-02-28"));

//        Date paramDate = DateUtil.parseDate("2022-02-01");
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(paramDate);
//        calendar.add(Calendar.DATE, -1);   // 昨天
//        Date yesterday = calendar.getTime();
//        System.out.print(yesterday.toString());
//        calendar.set(Calendar.DAY_OF_MONTH, 1);
//        calendar.add(Calendar.DATE, -1);
//        Date lyesterday=calendar.getTime();//上上月
//        System.out.print(lyesterday.toString());
//        int year=2022;
//        int month=10;
//
//        System.out.println(getMonthFullDay(year,month).toString());

        //获取上月天数
//        List<String> pmonthFullDay = DateUtil.getMonthFullDay(Integer.parseInt(dataDate.substring(0, 4)), Integer.parseInt(dataDate.substring(5,7))-1);
//        System.out.println(pmonthFullDay.toString());
//        //获取当月天数
//        List<String> cmonthFullDay= DateUtil.getMonthFullDay(Integer.parseInt(dataDate.substring(0, 4)), Integer.parseInt(dataDate.substring(5,7)));
//        System.out.println(cmonthFullDay.toString());
//        //获取日期最大的list
//        List<String> days=new ArrayList<>();
//        if(pmonthFullDay.size()>cmonthFullDay.size()){
//            days=pmonthFullDay;
//        }else{
//            days=cmonthFullDay;
//        }
//        System.out.println(days.toString());
//        List<String> dayList = days.stream().map(str -> {
//            return str.substring(8, 10);
//        }).collect(Collectors.toList());
//        System.out.println(dayList.toString());
	}


	/**
	 * @Title: getNoWeekDateList
	 * @Description: 获取过排除周六周天的日期集合
	 * @param @return 参数
	 * @return List<String> 返回类型
	 * @throws
	 */
	public static List<String> getNoWeekDateList(List<String> dateList){
		List<String> resultList = new ArrayList<String>();
		//然后将周六周天的时间过滤掉
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();

        for (String time: dateList) {
        	 try {
                 c.setTime(format.parse(time));
             } catch (ParseException e) {
                 e.printStackTrace();
             }

             int wek=c.get(Calendar.DAY_OF_WEEK);
             if (wek != 1 && wek != 7) {
				resultList.add(time);
			}
		}

		return resultList;
	}


	/**
	 * 根据时间获取年月日
	 * @Title: findDateBySplit
	 * @Description: 根据时间获取年月日
	 * @param @param date
	 * @param @return 参数
	 * @return Map<String,Integer> 年月日
	 * @throws
	 */
	public static Map<String, Integer> getNyr(String date){
		Map<String, Integer> map = new HashMap<String, Integer>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date parse = sdf.parse(date);
			String[] strNow = new SimpleDateFormat("yyyy-MM-dd").format(parse).toString().split("-");
			Integer year = Integer.parseInt(strNow[0]);
			Integer month = Integer.parseInt(strNow[1]);
			Integer day = Integer.parseInt(strNow[2]);
			map.put("year", year);
			map.put("month", month);
			map.put("day", day);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return map;
	}


	/**
	 * @Title: getNoWeekDateList
	 * @Description: 获取过排除周六周天的日期集合
	 * @param @return 参数
	 * @return List<String> 返回类型
	 * @throws
	 */
	public static List<String> getWeekDateList(List<String> dateList){
		List<String> resultList = new ArrayList<String>();
		//然后将周六周天的时间过滤掉
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();

        for (String time: dateList) {
        	 try {
                 c.setTime(format.parse(time));
             } catch (ParseException e) {
                 e.printStackTrace();
             }

             int wek=c.get(Calendar.DAY_OF_WEEK);
             if (wek == 1 || wek == 7) {
				resultList.add(time);
			}
		}

		return resultList;
	}

	/**
	 *	获取当月的第一天和最后一天日期
	 * @Title: getFilstLastDay
	 * @Description: TODO(获取当月的第一天和最后一天日期)
	 * @param @return 参数
	 * @return Map<String,String> 返回类型
	 * @throws
	 */
	public static Map<String, String> getflDay(){
		Map<String, String> map = new HashMap<String, String>();
		Calendar cale = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        // 获取前月的第一天
        cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 0);
        cale.set(Calendar.DAY_OF_MONTH, 1);
        map.put("firstday", format.format(cale.getTime()));
        // 获取前月的最后一天
        cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 1);
        cale.set(Calendar.DAY_OF_MONTH, 0);
        map.put("lastday", format.format(cale.getTime()));
        return map;
	}

	/**
	 *	根据一个日期获取它的第一天及最后一天
	 * @Title: getFirstDayDateOfMonth
	 * @Description: TODO(根据一个日期获取它的第一天及最后一天)
	 * @param @param date
	 * @param @return 参数
	 * @return Map<String,String> 返回类型
	 * @throws
	 */
	public static Map<String, String> getFirstDayDateOfMonth(String date) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        final Calendar cal = Calendar.getInstance();
			cal.setTime(sdf.parse(date));
	        final int first = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
	        cal.set(Calendar.DAY_OF_MONTH, first);
	        // 当月第一天
	        map.put("firstday", sdf.format(cal.getTime()));

	        final int last = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	        cal.set(Calendar.DAY_OF_MONTH, last);
	        // 当月最后一天
	        map.put("lastday", sdf.format(cal.getTime()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
        return map;
    }

	/**
	 *	时间格式化为年月日
	 * @Title: dateFormt
	 * @Description: TODO(时间格式化为年月日)
	 * @param @return 参数
	 * @return String 返回类型
	 * @throws
	 */
	public static String dateFormt(String data) {
        data = data.replaceAll("/","-")
                .replaceAll("_","-");
		String strDateFormat = "yyyy-MM-dd";
		SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
		Date date = null;
		try {
			date = sdf.parse(data);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return sdf.format(date);
	}

    /**
     * 格式化年月，防止出现 2021-1的情况
     * @param data
     * @return
     */
    public static String dateFormt2ym(String data) {
        String strDateFormat = "yyyy-MM";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
        Date date = null;
        try {
            date = sdf.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return sdf.format(date);
    }

	public static int getAge(Date birthDay) throws Exception {
        Calendar cal = Calendar.getInstance();
        if (cal.before(birthDay)) { //出生日期晚于当前时间，无法计算
            throw new IllegalArgumentException(
                    "The birthDay is before Now.It's unbelievable!");
        }
        int yearNow = cal.get(Calendar.YEAR);  //当前年份
        int monthNow = cal.get(Calendar.MONTH);  //当前月份
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH); //当前日期
        cal.setTime(birthDay);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
        int age = yearNow - yearBirth;   //计算整岁数
        if (monthNow <= monthBirth) {
	        if (monthNow == monthBirth) {
	            if (dayOfMonthNow < dayOfMonthBirth) age--;//当前日期在生日之前，年龄减一
	        }else{
	            age--;//当前月份在生日之前，年龄减一
	        }
        }
        return age;
	}

	/**
	 *	获取近半年年月
	 * @Title: getSixMonth
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param @param date
	 * @param @return 参数
	 * @return List<String> 返回类型
	 * @throws
	 */
	public static List<String> getSixMonth(String date) {
        //返回值
        List<String> list = new ArrayList<String>();
        int month = Integer.parseInt(date.substring(5, 7));
        int year = Integer.parseInt(date.substring(0, 4));
        for (int i = 5; i >= 0; i--) {
            if (month > 6) {
                if (month - i >= 10) {
                    list.add(year + "-" + String.valueOf(month - i));
                } else {
                    list.add(year + "-0" + String.valueOf(month - i));
                }
            } else {
                if (month - i <= 0) {
                    if (month - i + 12 >= 10) {
                        list.add(String.valueOf(year - 1) + "-" + String.valueOf(month - i + 12));
                    } else {
                        list.add(String.valueOf(year - 1) + "-0" + String.valueOf(month - i + 12));
                    }
                } else {
                    if (month - i >= 10) {
                        list.add(String.valueOf(year) + "-" + String.valueOf(month - i));
                    } else {
                        list.add(String.valueOf(year) + "-0" + String.valueOf(month - i));
                    }
                }
            }
        }
        return list;
    }


	/**
	 *	计算两个时间相差多少小时
	 * @param endTime 结束时间
	 * @param startTime 开始时间
	 * @return
	 */
	public static String getHour(String startTime,String endTime) throws Exception{

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date nowDate = format.parse(startTime);
		Date endDate = format.parse(endTime);

	    long nd = 1000 * 24 * 60 * 60;
	    long nh = 1000 * 60 * 60;

	    // long ns = 1000;
	    // 获得两个时间的毫秒时间差异
	    long diff = endDate.getTime() - nowDate.getTime();

	    // 计算差多少小时
	    long hour = diff / nh;

	    // 计算差多少秒//输出结果
	    // long sec = diff % nd % nh % nm / ns;
	    return hour + "";
	}


	/**
	 *	获取前day天日期
	 * @Title: getDateYW
	 * @Description: TODO(获取前day天日期)
	 * @param @return 参数
	 * @return Date 返回类型
	 * @throws
	 */
	public static Date getDateYW(int day) {
		Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, - day);
        return c.getTime();
	}

	/**
     *	判断当前时间是否在[startTime, endTime]区间，注意时间格式要一致
     *
     * @param nowTime 当前时间
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     * @author jqlin
     */
    public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
        if (nowTime.getTime() == startTime.getTime()
                || nowTime.getTime() == endTime.getTime()) {
            return true;
        }

        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *	获取一个时间的num天
     * @Title: findDateNum
     * @Description: TODO(获取一个时间的num天)
     * @param @param dateStr
     * @param @param num
     * @param @return 参数
     * @return String 返回类型
     * @throws
     */
    public static String findDateNum(String dateStr, int num) {
    	DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = format1.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, num);  // 在当前日基础上-1
		return format1.format(calendar.getTime());
    }

    /**
     *	获取本周的所有的时间区间
     * @param date
     * @return
     */
    public static Map<String,String> getTimeInterval(Date date) {
    	Map<String,String> result = new HashMap<String, String>();
    	SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD);

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 判断要计算的日期是否是周日，如果是则减一天计算周六的，否则会出问题，计算到下一周去了
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);// 获得当前日期是一个星期的第几天
        if (1 == dayWeek) {
           cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        // System.out.println("要计算日期为:" + sdf.format(cal.getTime())); // 输出要计算日期
        // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        // 获得当前日期是一个星期的第几天
        int day = cal.get(Calendar.DAY_OF_WEEK);
        // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
        String imptimeBegin = sdf.format(cal.getTime());
        // System.out.println("所在周星期一的日期：" + imptimeBegin);
        cal.add(Calendar.DATE, 6);
        String imptimeEnd = sdf.format(cal.getTime());
        result.put("start", imptimeBegin);
        result.put("end", imptimeEnd);
        return result;
    }

    /**
     *	获取指定日期的上月
     * @Title: getLastMonth
     * @Description: TODO(获取指定日期的上月)
     * @param @return 参数
     * @return String 返回类型
     * @throws
     */
    public static String getLastMonth(String data) {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
   	 	Calendar c = Calendar.getInstance();
   	 	try {
			c.setTime(sdf.parse(data));
			c.add(Calendar.MONTH, -1);
		} catch (ParseException e) {
			e.printStackTrace();
		}

   		return sdf.format(c.getTime());
    }

    /**
     *	获取指定日期的上月
     * @Title: getLastMonth
     * @Description: TODO(获取指定日期的上月)
     * @param @return 参数
     * @return String 返回类型
     * @throws
     */
    public static String getLastYear(String data,int num) {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
   	 	Calendar c = Calendar.getInstance();
   	 	try {
			c.setTime(sdf.parse(data));
			c.add(Calendar.YEAR, num);
		} catch (ParseException e) {
			e.printStackTrace();
		}

   		return sdf.format(c.getTime());
    }


   /**
    *	获取某个时间前后num小时的时候，以前的传num传负数
    * @param time
    * @param num
    * @return
    * @throws Exception
    */
    public static String getHourOffSet(String time,int num) throws Exception{
    	//获取前一小时的时间
    	SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
    	//这里设置你想要的时间
    	Date date=sdf.parse(time);
    	Calendar calendar = Calendar.getInstance(); calendar.setTime(date);
    	//前面设置的时间-1
    	calendar.add(Calendar.HOUR_OF_DAY, num);
    	date = calendar.getTime();
    	System.out.println(sdf.format(date));
    	return sdf.format(date);
    }

    /**
	 * yyyy-MM-dd HH:mm:ss转换为yyyy-MM-dd
	 * @author 刘鹏
	 * @throws ParseException
	 */
	public static Date transferFormat(Date tempDate) throws ParseException{
		SimpleDateFormat s1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat s2 = new SimpleDateFormat("yyyy-MM-dd");
		String outTime = null;
		outTime = s2.format(s2.parse(s1.format(tempDate)));
		return parseDate(outTime);

	}

	/**
	  *	时间格式转换，将字符串型时间转换为毫秒数
	  * "yyyy-MM-dd HH:mm:ss" => "12345"    19位
	  * "yyyyMMddHHmmss" => "12345"        14位
	  * "yyyy-MM-dd" => "12345"        10位
	  *	返回 0 是格式不对
	  * @param dateStr
	  * @return
	  */
	public static long parseStringToLong(String dateStr) {
		dateStr = dateStr.trim();
		if (dateStr.length() == 19) {
			try {
				Calendar cal = Calendar.getInstance();
				cal.set(Integer.parseInt(dateStr.substring(0, 4)), Integer.parseInt(dateStr.substring(5, 7)) - 1,
						Integer.parseInt(dateStr.substring(8, 10)), Integer.parseInt(dateStr.substring(11, 13)),
						Integer.parseInt(dateStr.substring(14, 16)), Integer.parseInt(dateStr.substring(17, 19)));
				cal.set(Calendar.MILLISECOND, 0);
				return (cal.getTime().getTime());
			} catch (Exception e) {
				return 0;
			}

		} else if (dateStr.length() == 16) {
			try {
				Calendar cal = Calendar.getInstance();
				cal.set(Integer.parseInt(dateStr.substring(0, 4)), Integer.parseInt(dateStr.substring(5, 7)) - 1,
						Integer.parseInt(dateStr.substring(8, 10)), Integer.parseInt(dateStr.substring(11, 13)),
						Integer.parseInt(dateStr.substring(14, 16)));
				cal.set(Calendar.MILLISECOND, 0);
				return (cal.getTime().getTime());
			} catch (Exception e) {
				return 0;
			}
		} else if (dateStr.length() == 14) {
			try {
				Calendar cal = Calendar.getInstance();
				cal.set(Integer.parseInt(dateStr.substring(0, 4)), Integer.parseInt(dateStr.substring(4, 6)) - 1,
						Integer.parseInt(dateStr.substring(6, 8)), Integer.parseInt(dateStr.substring(8, 10)),
						Integer.parseInt(dateStr.substring(10, 12)), Integer.parseInt(dateStr.substring(12, 14)));
				cal.set(Calendar.MILLISECOND, 0);
				return (cal.getTime().getTime());
			} catch (Exception e) {
				return 0;
			}
		} else if (dateStr.length() == 10) {
			try {
				Calendar cal = Calendar.getInstance();
				cal.set(Integer.parseInt(dateStr.substring(0, 4)), Integer.parseInt(dateStr.substring(5, 7)) - 1,
						Integer.parseInt(dateStr.substring(8, 10)), 0, 0, 0);
				cal.set(Calendar.MILLISECOND, 0);
				return (cal.getTime().getTime());
			} catch (Exception e) {
				return 0;
			}
		} else {
			try {
				return Long.parseLong(dateStr);
			} catch (Exception e) {
				return 0;
			}

		}

	}
	
	
	/**
	 * 验证时间字符串格式输入是否正确
	 * @param timeStr
	 * @return
	 */
	public static boolean valiDateTimeWithLongFormat(String timeStr) {
		String format = "((19|20)[0-9]{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])";
		Pattern pattern = Pattern.compile(format);
		Matcher matcher = pattern.matcher(timeStr);
		if (matcher.matches()) {
			pattern = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");
			matcher = pattern.matcher(timeStr);
			if (matcher.matches()) {
				int y = Integer.valueOf(matcher.group(1));
				int m = Integer.valueOf(matcher.group(2));
				int d = Integer.valueOf(matcher.group(3));
				if (d > 28) {
					Calendar c = Calendar.getInstance();
					c.set(y, m - 1, 1);
					int lastDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
					return (lastDay >= d);
				}
				return true;
			}
			return false;
		}
		return false;
	}
    /**
     获取指定日期上⽉最后⼀天
     *
     * @return
     */
    public static String getPrecedingMonthMaxDate(String tdate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(tdate));
            c.set(Calendar.DAY_OF_MONTH,0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
         String lastDay = sdf.format(c.getTime());
        return lastDay;
    }

    public static boolean isCellDateFormatted(Cell cell)
    {
        if (cell == null) return false;
        boolean bDate = false;

        double d = cell.getNumericCellValue();
        if (isValidExcelDate(d)) {
            CellStyle style = cell.getCellStyle();
            if (style == null) return false;
            int i = style.getDataFormat();
            String f = style.getDataFormatString();
            bDate = isADateFormat(i, f);
        }
        return bDate;
    }

    public static boolean isADateFormat(int formatIndex, String formatString)
    {
        if (isInternalDateFormat(formatIndex)) {
            return true;
        }

        if ((formatString == null) || (formatString.length() == 0)) {
            return false;
        }

        String fs = formatString;
        //下面这一行是自己手动添加的 以支持汉字格式wingzing
        fs = fs.replaceAll("[\"|\']","").replaceAll("[年|月|日|时|分|秒|毫秒|微秒]", "");

        fs = fs.replaceAll("\\\\-", "-");

        fs = fs.replaceAll("\\\\,", ",");

        fs = fs.replaceAll("\\\\.", ".");

        fs = fs.replaceAll("\\\\ ", " ");

        fs = fs.replaceAll(";@", "");

        fs = fs.replaceAll("^\\[\\$\\-.*?\\]", "");

        fs = fs.replaceAll("^\\[[a-zA-Z]+\\]", "");

        return (fs.matches("^[yYmMdDhHsS\\-/,. :]+[ampAMP/]*$"));
    }

    public static boolean isInternalDateFormat(int format)
    {
        switch (format) { case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 45:
            case 46:
            case 47:
                return true;
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44: } return false;
    }

    public static boolean isValidExcelDate(double value)
    {
        return (value > -4.940656458412465E-324D);
    }

    /**
     * @Title: DateUtil
     * @Description: 生成时间字符串
     * @Param: []
     * @return: java.lang.String
     * @throws:
     * @Author: Wanghonglin
     * @Date: 2022/6/27 15:16
     * @version: v1.0
     * Modification History:
     * @Author        @date        @version   @Description
     * Wanghonglin  2022/6/27 15:16    v1.0    生成时间字符串
     * -------------------------------------------------------------
     */
    public static String generateTimeStr() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(YYYYMMDDHHMMSS));
    }


    /**
     * 得到当前年
     * @return 年
     */
    public static int currentYear() {
        return new GregorianCalendar().get(Calendar.YEAR);
    }

    /**
     * 得到当前月
     * @return 月
     */
    public static int currentMonth() {
        return new GregorianCalendar().get(Calendar.MONTH) + 1;
    }

    /**
     * 得到当前日
     * @return 日
     */
    public static int currentDay() {
        return new GregorianCalendar().get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 得到日期中的日
     * @param date
     * @return
     */
    public static int getDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 得到日期中的月
     */
    public static int getMonth(Date date) {
        // "一月", "二月", "三月", "四月","五月", "六月", "七月", "八月","九月", "十月", "十一月", "十二月"
        int [] months = {1,2,3,4,5,6,7,8,9,10,11,12};
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return months[calendar.get(Calendar.MONTH)];
    }

    /**
     * 得到日期中的年
     * @param date 日期
     * @return
     */
    public static int getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }


    /**
     * 自定义格式化日期
     * @param date
     * @param pattern
     * @return
     */
    public static String format(Date date, String pattern) {
        SimpleDateFormat spf = new SimpleDateFormat(pattern);
        return date != null? spf.format(date) : "";
    }
    //获取前一天的日期
    public static Date getPerDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        date = calendar.getTime();
        return date;
    }
}
