# 코루틴 개발 환경 설정

#### `Thread.currentThread().name` 에 현재 코루틴 이름까지 나오게 설정하는 방법
1. Edit Configurations.. 클릭
2. VM Options 에 -Dkotlinx.coroutines.debug 추가


이런식으로 나옴
```kotlin
[main @coroutine#1]
```

#### 코루틴 이름 지정하기
```kotlin
launch(context = CoroutineName("myCoroutine")) { ... }
```
