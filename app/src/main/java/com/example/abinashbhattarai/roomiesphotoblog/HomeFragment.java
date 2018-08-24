package com.example.abinashbhattarai.roomiesphotoblog;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blogPostView;
    private List<BlogPost> blogPostList;
    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    FirebaseAuth firebaseAuth;
    private boolean isFirstPageFirstLoad=true;
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        blogPostView = view.findViewById(R.id.blogListView);

        blogPostList = new ArrayList<>();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blogPostList);
        blogPostView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        blogPostView.setAdapter(blogRecyclerAdapter);
        blogPostView.setHasFixedSize(true);
        if (firebaseAuth.getCurrentUser() != null) {
            blogPostView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    Boolean reachedBottom= !recyclerView.canScrollVertically(1);
                    if(reachedBottom){
                        String desc=lastVisible.getString("description");
                        Toast.makeText(container.getContext(), "Reached : " + desc, Toast.LENGTH_SHORT).show();
                        addMorepost();
                    }
                }
            });
            Query firstQuery=firebaseFirestore.collection("Post").orderBy("timestamp",Query.Direction.DESCENDING).limit(3);

            firstQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    Log.i("Document", "Added");
                    if(isFirstPageFirstLoad) {
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                    }

                    if (!documentSnapshots.isEmpty()) {
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) { //This looks for the document changes


                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String blogPostId=doc.getDocument().getId();

                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId); //Convert the document to BlogPost type
                                if(isFirstPageFirstLoad){

                                    blogPostList.add(blogPost);}
                                else{
                                    blogPostList.add(0,blogPost);
                                }
                                //Everytime new data is added blogPostList gets the data and displays in the RecyclerView in BlogRecyclerAdapter class

                                blogRecyclerAdapter.notifyDataSetChanged(); //We need to notify adapter that the data has been changed


                            }

                        }
                        isFirstPageFirstLoad=false;
                    }
                }
            });

        }
            return view;
        }

        public void addMorepost(){
            Query nextQuery=firebaseFirestore.collection("Post")
                    .orderBy("timestamp",Query.Direction.DESCENDING).
                            startAt(lastVisible).limit(2);

            nextQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    Log.i("Document", "Added");
                    lastVisible=documentSnapshots.getDocuments().get(documentSnapshots.size()-1);



                    if (!documentSnapshots.isEmpty()) {
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) { //This looks for the document changes


                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String blogPostId=doc.getDocument().getId();

                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                //Convert the document to BlogPost type
                                blogPostList.add(blogPost);
                                //Everytime new data is added blogPostList gets the data and displays in the RecyclerView in BlogRecyclerAdapter class

                                blogRecyclerAdapter.notifyDataSetChanged(); //We need to notify adapter that the data has been changed


                            }

                        }
                    }
                }
            });


        }


}


