import java.util.Scanner;

import static java.lang.System.in;
import static java.lang.System.out;

public class Example
{
    public static void main(String[] args) {
        Scanner scanner = new Scanner(in);
        int i = scanner.nextInt();
        int j = scanner.nextInt();
        int t = scanner.nextInt();

        if((i+j+t) % 2 == 0)
        {
            if (i < j)
                if (i < t)
                    out.println(i);
                else
                    out.println(t);
            else
                if(j < t)
                    out.println(j);
                else
                    out.println(t);
        }
        else out.println(i+j+t);
    }

}

class Example2 {
    public static void main(String[] args) {
        String ch = new Scanner(in).nextLine();
        switch(ch) {
            case "a":
            case "e":
            case "i":
            case "o":
            case "u":
                out.println("모음입니다.");
                break;
            default:
                out.println("자음입니다.");
        }
    }
}

class Example3 {
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(in);
        out.println("년도를 입력하세요");
        int year = scanner.nextInt();
        if(year % 4 == 0)
                if(year % 100 != 0)
                    out.println(year + "년은 윤년입니다.");
                else
                    out.println(year + "년은 평년입니다.");
        else
            if(year % 100 == 0)
                if(year % 400 == 0)
                    out.println(year + "년은 윤년입니다.");
                else
                    out.println(year + "년은 평년입니다.");
            else
                out.println(year + "년은 평년입니다.");
    }
}