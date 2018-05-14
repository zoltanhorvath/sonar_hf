package app.outlay.firebase;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import javax.inject.Inject;

import app.outlay.domain.model.User;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by bmelnychuk on 10/26/16.
 */

public class FirebaseAuthRxWrapper {
    private FirebaseAuth firebaseAuth;

    @Inject
    public FirebaseAuthRxWrapper(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    public Observable<String> getUserToken(FirebaseUser firebaseUser) {
        return Observable.create(subscriber -> {
            Task<GetTokenResult> task = firebaseUser.getToken(true);

            task.addOnCompleteListener(resultTask -> {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    subscriber.onNext(token);
                    subscriber.onCompleted();
                } else {
                    Exception e = task.getException();
                    subscriber.onError(e);
                }
            });
        });
    }

    public Observable<AuthResult> signUp(String email, String password) {
        return Observable.create(subscriber -> {
            Task<AuthResult> task = firebaseAuth.createUserWithEmailAndPassword(email, password);
            addCompleteListenerToTask(subscriber, task);
        });
    }

    public Observable<AuthResult> signIn(String email, String password) {
        return Observable.create(subscriber -> {
            Task<AuthResult> task = firebaseAuth.signInWithEmailAndPassword(email, password);
            addCompleteListenerToTask(subscriber, task);
        });
    }

    public Observable<AuthResult> signInAnonymously() {
        return Observable.create(subscriber -> {
            Task<AuthResult> task = firebaseAuth.signInAnonymously();
            addCompleteListenerToTask(subscriber, task);
        });
    }

    private void addCompleteListenerToTask(Subscriber<? super AuthResult> subscriber, Task<AuthResult> task) {
        task.addOnCompleteListener(resultTask -> {
            if (task.isSuccessful()) {
                AuthResult authResult = task.getResult();
                subscriber.onNext(authResult);
                subscriber.onCompleted();
            } else {
                Exception e = task.getException();
                subscriber.onError(e);
            }
        });
    }

    public Observable<AuthResult> linkAccount(AuthCredential credentials) {
        return Observable.create(subscriber -> {
            Task<AuthResult> task = firebaseAuth.getCurrentUser().linkWithCredential(credentials);
            addCompleteListenerToTask(subscriber, task);
        });
    }


    public Observable<Void> resetPassword(User user) {
        return Observable.create(subscriber -> {
            Task<Void> task = firebaseAuth.sendPasswordResetEmail(user.getEmail());
            task.addOnCompleteListener(resultTask -> {
                if (task.isSuccessful()) {
                    subscriber.onCompleted();
                } else {
                    Exception e = task.getException();
                    subscriber.onError(e);
                }
            });
        });
    }
}
