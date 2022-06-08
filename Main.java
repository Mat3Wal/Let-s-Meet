package leetsMeet;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        CalendarOperations operations=new CalendarOperations();
        UserCalendar user1=new UserCalendar("09:00","19:55");
        UserCalendar user2=new UserCalendar("10:00","18:30");
        Meeting newMeeting;

        user1.todaysMeetings.add(newMeeting=new Meeting("09:00","10:30"));
        user1.todaysMeetings.add(newMeeting=new Meeting("12:00","13:00"));
        user1.todaysMeetings.add(newMeeting=new Meeting("16:00","18:00"));

        user2.todaysMeetings.add(newMeeting=new Meeting("10:00","11:30"));
        user2.todaysMeetings.add(newMeeting=new Meeting("12:30","14:30"));
        user2.todaysMeetings.add(newMeeting=new Meeting("14:30","15:00"));
        user2.todaysMeetings.add(newMeeting=new Meeting("16:00","17:00"));

        System.out.println("Potencjalne godziny spotkań: ");
        operations.findPossibleMeetingTime(user1,user2).forEach(operation->System.out.print("["+operation.meetingStart+", "+operation.meetingEnd+"]  "));

    }

}

class Meeting{
    String meetingStart;
    String meetingEnd;
    Meeting(String start, String end){
        this.meetingStart=start;
        this.meetingEnd=end;
    }
}

class UserCalendar{
    String startOfWork;
    String endOfWork;
    ArrayList <Meeting> todaysMeetings=new ArrayList<>();

    UserCalendar(String startOfWork, String endOfWork){
        this.startOfWork=startOfWork;
        this.endOfWork=endOfWork;
    }
}

class CalendarOperations {

    private int timeToMinutes (String time){
        final String[] parts = time.split(":");
        final Integer timeInMinutes = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        return timeInMinutes;
    }

    private int compareTime(String end, String start) {
        final Integer time1 = timeToMinutes(end);
        final Integer time2 = timeToMinutes(start);

        if (time1 > time2)
            return 1;
        else if (time2 > time1)
            return -1;
        else
            return 0;
    }
    // Metoda ustalająca ramy czasowe, w których w ogóle możliwe jest wyszukiwanie bloków czasu o postawionych wymaganiach
    private Meeting setTimeFrame (UserCalendar user1,UserCalendar user2 ){
        final String start,end;
        Meeting timeFrame;

        if(compareTime(user1.startOfWork, user2.startOfWork)==1)
            start=user1.startOfWork;
        else if(compareTime(user1.startOfWork, user2.startOfWork)==-1)
            start=user2.startOfWork;
        else
            start=user1.startOfWork;

        if(compareTime(user1.endOfWork, user2.endOfWork)==-1){
            end=user1.endOfWork;
        }else if(compareTime(user1.endOfWork, user2.endOfWork)==1)
            end=user2.endOfWork;
        else
            end=user1.endOfWork;
        return timeFrame=new Meeting(start,end);
    }
    //Sprawdzenie czy znaleziony blok czasu ma wystarczający rozmiar, by można było umówić spotkanie
    private boolean isEnoughTime (String end, String start){
        if(timeToMinutes(end)-timeToMinutes(start)>=30){
            return true;
        }else
           return false;
    }
    //Stworzenie wspólnego kalendarza w postaci sortowanej listy z danych wejściowych
    private ArrayList<Meeting> mergeCalendars(UserCalendar user1, UserCalendar user2) {
        ArrayList<Meeting> mergedCalendar = new ArrayList<>();
        int i = 0, j = 0;

        while (mergedCalendar.size() != (user1.todaysMeetings.size() + user2.todaysMeetings.size())) {

            //Jeżeli wszystkie spotkania z jedenego z kalendarzy zostały już dodane -> dodaj pozostałe spotkania z drugiego do wspólnej listy
            if (i <= user1.todaysMeetings.size() - 1) {
                if (j <= user2.todaysMeetings.size() - 1) {

                    //Instrukcje porównywujące godziny rozpoczęcia spotkań w obu kalendarzach, dodając kolejne spotkania rosnąco względem daty rozpoczęcia spotkania do wspólnej listy
                    if (compareTime(user1.todaysMeetings.get(i).meetingStart, user2.todaysMeetings.get(j).meetingStart) == -1) {
                        mergedCalendar.add(user1.todaysMeetings.get(i));
                        i++;

                    }else if (compareTime(user1.todaysMeetings.get(i).meetingStart, user2.todaysMeetings.get(j).meetingStart) == 0) {

                        if(compareTime(user1.todaysMeetings.get(i).meetingEnd,user2.todaysMeetings.get(j).meetingEnd)==1){
                            mergedCalendar.add(user2.todaysMeetings.get(j));
                            mergedCalendar.add(user1.todaysMeetings.get(i));
                            i++;
                            j++;
                        }else{
                            mergedCalendar.add(user1.todaysMeetings.get(i));
                            mergedCalendar.add(user2.todaysMeetings.get(j));
                            i++;
                            j++;
                        }
                    } else {
                        mergedCalendar.add(user2.todaysMeetings.get(j));
                        j++;}
                } else {
                    mergedCalendar.add(user1.todaysMeetings.get(i));
                    if (i < user1.todaysMeetings.size() - 1) {
                        i++;
                    }
                }
            } else {
                mergedCalendar.add(user2.todaysMeetings.get(j));
                if (j < user2.todaysMeetings.size() - 1) {
                    j++;
                }
            }
        }
        return mergedCalendar;
    }

    ArrayList<Meeting> findPossibleMeetingTime (UserCalendar user1, UserCalendar user2 ){

        ArrayList<Meeting> finalCalendar = new ArrayList<>();
        ArrayList<Meeting> mergedCalendar=mergeCalendars(user1,user2);
        Meeting possibleMeeting;
        final Meeting timeFrame=setTimeFrame(user1,user2);
        final String startOfDayWork, endOfDayWork;
        startOfDayWork=timeFrame.meetingStart;
        endOfDayWork=timeFrame.meetingEnd;

        int temp=0;

        while (temp<mergedCalendar.size()-1){

/*          Kod mający na celu zmodyfikowanie posortowanej listy wszystkich spotkań z obu kalendarzy.
            W celu uzyskania wolnych bloków na potencjalne spotkanie dwóch osób, sprawdzana jest zależność czy z pary godzin
            [i.koniecSpotkania; (i+1).startSpotkania] koniec następuje później niż początek następnego sptokania na liście.
            Jeżeli ten warunek jest spełniony początowi następnego spotkania (i+1).startSpotkania przypisana zostaje watrtość i.koniecSpotkania.
            W taki sposób widać "linie czasu", w której przerwy są porządaną odpowiedzią czyli  wolnym blokiem będącym częścią wspólną obu kalendarzy */
            if(compareTime(mergedCalendar.get(temp).meetingEnd,mergedCalendar.get(temp+1).meetingStart)==1){
                mergedCalendar.get(temp+1).meetingStart=mergedCalendar.get(temp).meetingEnd;
            }
            temp++;
        }
        temp=0;

//        Sprawdzenie i dodanie czy można umówić spotkanie przez podanymi spotkaniami na wejściu
        if(compareTime(mergedCalendar.get(0).meetingStart,startOfDayWork)==1)
            finalCalendar.add(possibleMeeting=new Meeting(startOfDayWork,mergedCalendar.get(0).meetingStart));

        while(temp<mergedCalendar.size()-1){

/*            Sprawdzanie par [i.koniecSpotkania; (i+1).startSpotkania] jeżeli start następnego jest później niż koniec aktualnego i różnica większa od 30
             -> stworzenie nowego spotkania i dodanie do listy potencjalnych spotkań */
            if(compareTime(mergedCalendar.get(temp).meetingEnd, mergedCalendar.get(temp+1).meetingStart)==-1){
                if(isEnoughTime(mergedCalendar.get(temp+1).meetingStart,mergedCalendar.get(temp).meetingEnd)){
                    finalCalendar.add(possibleMeeting=new Meeting(mergedCalendar.get(temp).meetingEnd,mergedCalendar.get(temp+1).meetingStart));
                }
            }
            temp++;
        }

/*        Sprawdzenie czy po ostatnim spotkaniu na połączonej liście jest możliwe stworzenie spotkania-
        [ostatnieSpotkanie.End; wcześniejszy koniec pracy odczytany z obu kalendarzy]. Jeżeli tak następuje dodanie do listy potencjalnych spotkań*/
        if(compareTime(endOfDayWork,mergedCalendar.get((mergedCalendar.size()-1)).meetingEnd)==1)
            finalCalendar.add(possibleMeeting=new Meeting(mergedCalendar.get((mergedCalendar.size()-1)).meetingEnd,endOfDayWork));

        return finalCalendar;
    }
}
