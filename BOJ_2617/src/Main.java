import java.io.*;
import java.util.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static int N,M;
    static List<Integer>[] small;
    static List<Integer>[] big;
    static boolean[] check;

    public static void main(String[] args) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());

        small = new List[N+1];
        big = new List[N+1];


        for(int i=1; i<N+1; i++){
            small[i] = new ArrayList<>();
            big[i] = new ArrayList<>();
        }

        for(int i=0; i<M; i++){
            st = new StringTokenizer(br.readLine());
            int a = Integer.parseInt(st.nextToken());
            int b = Integer.parseInt(st.nextToken());
            small[b].add(a); // b보다 작은 것은 a 다
            big[a].add(b); // a 보다 큰 것은 b 다
        }


        int cnt = (N+1) / 2;
        int ans = 0;

        for(int i=1; i<N+1; i++){
            int s = dfs(i,small);
            int b = dfs(i,big);
            if(s >= cnt || b >= cnt) ans++;
        }


        System.out.println(ans);



    }

    static int dfs(int start,List[] list){

    }
}