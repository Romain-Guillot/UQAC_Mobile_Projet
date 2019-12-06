package com.progmobile.meetchup.ui.event_view.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.progmobile.meetchup.R;
import com.progmobile.meetchup.models.Post;
import com.progmobile.meetchup.models.User;
import com.progmobile.meetchup.repositories.FirebaseStorageRepository;
import com.progmobile.meetchup.repositories.IStorageRepository;
import com.progmobile.meetchup.ui.post_view.PostViewActivity;
import com.progmobile.meetchup.utils.Callback;
import com.progmobile.meetchup.utils.CallbackException;
import com.progmobile.meetchup.utils.DownloadImage;

import java.util.List;


/**
 *
 */
public class PostsListViewAdapter extends RecyclerView.Adapter<PostsListViewAdapter.PostViewHolder> {

    private static IStorageRepository storageRepository;
    private List<Post> posts;

    public PostsListViewAdapter(List<Post> posts) {
        this.posts = posts;
        storageRepository = FirebaseStorageRepository.getInstance();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView descriptionView;
        ImageView imageView;
        TextView userView;
        Context context;

        PostViewHolder(View itemView) {
            super(itemView);
            this.descriptionView = itemView.findViewById(R.id.item_post_description);
            this.imageView = itemView.findViewById(R.id.item_post_image);
            this.userView = itemView.findViewById(R.id.item_post_user);
            context = itemView.getContext();
        }

        public void bind(Post post) {
            String description = post.getDescription();
            if (description != null)
                descriptionView.setText(post.getDescription());

            imageView.setVisibility(View.GONE);
            String docURL = post.getDocUrl();
            if (docURL != null) {
                PostsListViewAdapter.storageRepository.getData(docURL, new Callback<byte[]>() {
                    public void onSucceed(byte[] result) {
                        imageView.setVisibility(View.VISIBLE);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(result, 0, result.length);
                        imageView.setImageBitmap(bitmap);
                    }
                    public void onFail(CallbackException exception) {

                    }
                });
            }

            User user = post.getUser();
            if (user == null || user.getName() == null) {
                userView.setText(context.getString(R.string.unknown_user));
            } else {
                String name = user.getName();
                userView.setText(name);
            }

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), PostViewActivity.class);
                intent.putExtra(PostViewActivity.EXTRA_POST_ID, post.getId());
                itemView.getContext().startActivity(intent);
            });
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View postView = inflater.inflate(R.layout.item_post, parent, false);
        PostViewHolder holder = new PostViewHolder(postView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

}
