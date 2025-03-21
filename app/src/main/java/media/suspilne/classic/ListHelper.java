package media.suspilne.classic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListHelper {
    public static List<String> removeBlank(String[] array){
        return removeBlank(Arrays.asList(array));
    }

    public static List<String> removeBlank(List<String> list){
        List<String> result = new ArrayList<String>();

        for (String item: list) {
            if (item != null && !item.equals(""))
                result.add(item);
        }

        return result;
    }

    public static ArrayList<Integer> intersect(ArrayList<Integer> list1, ArrayList<Integer> list2){
        ArrayList<Integer> result = new ArrayList<>();

        for(Integer id:list1){
            if (list2.contains(id)){
                result.add(id);
            }
        }

        return result;
    }

    public static ArrayList<Integer> union(ArrayList<Integer> list1, ArrayList<Integer> list2){
        ArrayList<Integer> result = new ArrayList<>(list1);

        for(Integer id:list2){
            if (!list1.contains(id)){
                result.add(id);
            }
        }

        return result;
    }
}