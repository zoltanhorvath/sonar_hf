package app.outlay.firebase;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.outlay.data.source.CategoryDataSource;
import app.outlay.domain.model.Category;
import app.outlay.domain.model.User;
import app.outlay.firebase.dto.CategoryDto;
import app.outlay.firebase.dto.adapter.CategoryAdapter;
import rx.Observable;

/**
 * Created by bmelnychuk on 10/26/16.
 */

public class CategoryFirebaseSource implements CategoryDataSource {
    private static final String USERS = "users";
    private static final String CATEGORIES = "categories";
    private DatabaseReference mDatabase;
    private CategoryAdapter adapter;
    private User currentUser;

    public CategoryFirebaseSource(
            User currentUser,
            DatabaseReference databaseReference
    ) {
        this.currentUser = currentUser;
        mDatabase = databaseReference;
        adapter = new CategoryAdapter();
    }

    @Override
    public Observable<List<Category>> getAll() {
        return Observable.create(subscriber -> mDatabase.child(USERS).child(currentUser.getId()).child(CATEGORIES).orderByChild("order")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Category> categories = new ArrayList<>();
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            CategoryDto categoryDto = postSnapshot.getValue(CategoryDto.class);
                            categories.add(adapter.toCategory(categoryDto));
                        }
                        subscriber.onNext(categories);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        subscriber.onError(databaseError.toException());
                    }
                }));
    }

    @Override
    public Observable<Category> getById(String id) {
        return getDtoById(id).map(categoryDto -> adapter.toCategory(categoryDto));
    }

    private Observable<CategoryDto> getDtoById(String id) {
        return Observable.create(subscriber -> mDatabase.child(USERS).child(currentUser.getId()).child(CATEGORIES).child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        CategoryDto categoryDto = dataSnapshot.getValue(CategoryDto.class);
                        subscriber.onNext(categoryDto);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        subscriber.onError(databaseError.toException());
                    }
                }));
    }

    @Override
    public Observable<List<Category>> updateOrder(List<Category> categories) {
        return Observable.create(subscriber -> {
            Map<String, Object> childUpdates = new HashMap<>();
            for (Category c : categories) {
                childUpdates.put(c.getId() + "/order", c.getOrder());
            }

            DatabaseReference categoriesRef = mDatabase.child(USERS).child(currentUser.getId()).child(CATEGORIES);
            categoriesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    subscriber.onNext(categories);
                    subscriber.onCompleted();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    subscriber.onError(databaseError.toException());
                }
            });
            categoriesRef.updateChildren(childUpdates);
        });
    }

    @Override
    public Observable<Category> save(Category category) {
        return Observable.create(subscriber -> {
            String key = category.getId();
            if (TextUtils.isEmpty(key)) {
                key = mDatabase.child(USERS).child(currentUser.getId()).child(CATEGORIES).push().getKey();
                category.setId(key);
            }

            Map<String, Object> childUpdates = getStringObjectMap(category);

            DatabaseReference dbRef = mDatabase
                    .child(USERS)
                    .child(currentUser.getId())
                    .child(CATEGORIES).child(key);

            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    CategoryDto categoryDto = dataSnapshot.getValue(CategoryDto.class);
                    if (categoryDto != null) {
                        subscriber.onNext(adapter.toCategory(categoryDto));
                        subscriber.onCompleted();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    subscriber.onError(databaseError.toException());
                }
            });

            dbRef.updateChildren(childUpdates);
        });
    }

    @NonNull
    private Map<String, Object> getStringObjectMap(Category category) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("id", category.getId());
        childUpdates.put("title", category.getTitle());
        childUpdates.put("icon", category.getIcon());
        childUpdates.put("color", category.getColor());
        childUpdates.put("order", category.getOrder());
        return childUpdates;
    }

    @Override
    public Observable<Category> remove(Category category) {
        return Observable.create(subscriber -> {
            DatabaseReference catReference = mDatabase.child(USERS).child(currentUser.getId())
                    .child(CATEGORIES).child(category.getId());
            catReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    subscriber.onNext(category);
                    subscriber.onCompleted();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    subscriber.onError(databaseError.toException());
                }
            });
            catReference.removeValue();
        });
    }
}
