package com.curatedblogs.app.interfaces;


import com.parse.ParseObject;

import java.util.List;

public interface IParseQueryRunner<T extends ParseObject> {
    void onComplete(List<T> result);
}
