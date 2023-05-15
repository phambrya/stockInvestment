import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class testingCode {
    public static void main(String[] args) {
        String date1 = "1998.01.02";
        String date2 = "1998.01.10";
        String date3 = "1998.01.02";
        System.out.println(date1.compareTo(date2) == -1);
        System.out.println(date1.compareTo(date3));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        try {
            Date d1 = sdf.parse(date1);
            Date d2 = sdf.parse(date2);

            long difference_In_Time = d2.getTime() - d1.getTime();
            long difference_In_Days = (difference_In_Time / (1000 * 60 * 60 * 24)) % 365;
            System.out.println(difference_In_Days);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        

    }

}