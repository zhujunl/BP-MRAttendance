package com.miaxis.attendance.ui.manager;

import com.miaxis.attendance.App;
import com.miaxis.attendance.data.entity.Face;
import com.miaxis.attendance.data.entity.Finger;
import com.miaxis.attendance.data.entity.LocalImage;
import com.miaxis.attendance.data.entity.Person;
import com.miaxis.attendance.data.model.FaceModel;
import com.miaxis.attendance.data.model.FingerModel;
import com.miaxis.attendance.data.model.LocalImageModel;
import com.miaxis.attendance.data.model.PersonModel;
import com.miaxis.attendance.data.model.page.BasePage;
import com.miaxis.common.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ManagerViewModel extends ViewModel {

    public MutableLiveData<List<MxUser>> MxUserList = new MutableLiveData<>();
    public MutableLiveData<Integer> PagerIndex = new MutableLiveData<>(1);
    public MutableLiveData<Boolean> NextEnable = new MutableLiveData<>();
    public MutableLiveData<Boolean> PreviousEnable = new MutableLiveData<>();

    public MutableLiveData<String> ErrorMsg = new MutableLiveData<>();
    private final int pageSize = 6;

    public ManagerViewModel() {
    }

    public void previous() {
        Disposable subscribe = Observable.create((ObservableOnSubscribe<List<MxUser>>) emitter -> {
            BasePage<Person> userPage = PersonModel.findPage(pageSize, decrementAndGet());
            List<MxUser> list = new ArrayList<>();
            List<Person> page = userPage.pageData();
            if (!ListUtils.isNullOrEmpty(page)) {
                for (Person person : page) {
                    list.add(new MxUser(
                            person.UserId,
                            person.Name,
                            person.Number,
                            findFaceImagePath(person),
                            findFingerImagePath(person)
                    ));
                }
            }
            PreviousEnable.postValue(userPage.position() > 1);
            NextEnable.postValue(userPage.position() < userPage.getTotal());
            if (ListUtils.isNullOrEmpty(list)) {
                throw new Exception("没有数据了");
            } else {
                emitter.onNext(list);
            }
        }).subscribeOn(Schedulers.from(App.getInstance().threadExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    MxUserList.setValue(list);
                }, throwable -> {
                    incrementAndGet();
                    ErrorMsg.setValue("" + throwable);
                });
    }

    public void next() {
        Disposable subscribe = Observable.create((ObservableOnSubscribe<List<MxUser>>) emitter -> {
            BasePage<Person> userPage = PersonModel.findPage(pageSize, incrementAndGet());
            List<MxUser> list = new ArrayList<>();
            List<Person> page = userPage.pageData();
            if (!ListUtils.isNullOrEmpty(page)) {
                for (Person person : page) {
                    list.add(new MxUser(
                            person.UserId,
                            person.Name,
                            person.Number,
                            findFaceImagePath(person),
                            findFingerImagePath(person)
                    ));
                }
            }
            PreviousEnable.postValue(userPage.position() > 1);
            NextEnable.postValue(userPage.position() < userPage.getTotal());
            if (ListUtils.isNullOrEmpty(list)) {
                throw new Exception("没有数据了");
            } else {
                emitter.onNext(list);
            }
        }).subscribeOn(Schedulers.from(App.getInstance().threadExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    MxUserList.setValue(list);
                }, throwable -> {
                    decrementAndGet();
                    ErrorMsg.setValue("" + throwable);
                });
    }

    private int decrementAndGet() {
        Integer value = this.PagerIndex.getValue();
        if (value == null) {
            value = 0;
        }
        this.PagerIndex.postValue(--value);
        return value;
    }

    private int incrementAndGet() {
        Integer value = this.PagerIndex.getValue();
        if (value == null) {
            value = 0;
        }
        this.PagerIndex.postValue(++value);
        return value;
    }

    private int normalGet() {
        Integer value = this.PagerIndex.getValue();
        if (value == null) {
            value = 0;
        }
        return value;
    }

    private String findFaceImagePath(Person person) {
        String facePath = null;
        if (!ListUtils.isNullOrEmpty(person.faceIds)) {
            Face face = FaceModel.findByID(person.faceIds.get(0));
            if (face != null) {
                List<LocalImage> faceImages = LocalImageModel.findByID(face.faceImageId);
                if (!ListUtils.isNullOrEmpty(faceImages)) {
                    facePath = faceImages.get(0).LocalPath;
                }
            }
        }
        return facePath;
    }

    private List<String> findFingerImagePath(Person person) {
        List<String> fingerPathList = new ArrayList<>();
        if (!ListUtils.isNullOrEmpty(person.fingerIds)) {
            for (Long id : person.fingerIds) {
                Finger finger = FingerModel.findByID(id);
                if (finger != null) {
                    List<LocalImage> faceImages = LocalImageModel.findByID(finger.fingerImageId);
                    if (!ListUtils.isNullOrEmpty(faceImages)) {
                        fingerPathList.add(faceImages.get(0).LocalPath);
                    }
                }
            }
        }
        return fingerPathList;
    }

    public void flush() {
        Disposable subscribe = Observable.create((ObservableOnSubscribe<List<MxUser>>) emitter -> {
            BasePage<Person> userPage = PersonModel.findPage(pageSize, normalGet());
            List<MxUser> list = new ArrayList<>();
            List<Person> page = userPage.pageData();
            if (!ListUtils.isNullOrEmpty(page)) {
                for (Person person : page) {
                    list.add(new MxUser(
                            person.UserId,
                            person.Name,
                            person.Number,
                            findFaceImagePath(person),
                            findFingerImagePath(person)
                    ));
                }
            }
            PreviousEnable.postValue(userPage.position() > 1);
            NextEnable.postValue(userPage.position() < userPage.getTotal());
            emitter.onNext(list);
        }).subscribeOn(Schedulers.from(App.getInstance().threadExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    MxUserList.setValue(list);
                }, throwable -> {
                    ErrorMsg.setValue("" + throwable);
                });
    }
}