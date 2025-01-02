# CoroutineDispatcher

## CoroutineDispatcher 란?
CoroutineDispatcher 객체는 코루틴을 스레드로 보내 실행하는 객체이다.  
작업 대기열에 적재한 후 사용이 가능한 스레드로 보내 실행한다.  

## 제한된 디스패처와 무제한 디스패처
디스패처의 종류에는 크게 제한된 디스패처, 무제한 디스패처가 있다.
1. 제한된 디스패처 : 특정 스레드 또는 스레드풀로 제한된다.
2. 무제한 디스패처 : 코루틴을 실행하는 데 사용할 수 있는 스레드가 제한되지 않는다.

객채별로 어떤 작업을 처리할지 미리 역할을 부여하고 역할에 맞춰 실행을 요청하는 것이 효율적이기 때문에 대부분 제한된 디스패처를 사용한다.  


## 제한된 디스패처 생성하기
newSingleThreadContext 및 newFixedThreadPoolContext 함수를 사용해 제한된 디스패처 객체를 생성할 수 있다.
```kotlin
// 단일 스레드 디스패처 만들기
val singleThreadDispatcher: CoroutineDispatcher = newSingleThreadContext(name = "SingleThread")
launch(context = singleThreadDispatcher) { name() }
```
```kotlin
// 출력
[SingleThread @coroutine#2]
```
---
```kotlin
// 멀티 스레드 디스패처 만들기
val multiThreadDispatcher: CoroutineDispatcher = newFixedThreadPoolContext(
        nThreads = 2,
        name = "MultiThread"
    )
repeat(3){
    launch(context = multiThreadDispatcher) { name() }
}
```
```kotlin
// 출력
[MultiThread-1 @coroutine#2]
[MultiThread-1 @coroutine#3]
[MultiThread-2 @coroutine#4]
```

## 자식 코루틴은 기본적으로 부모 코루틴의 CoroutineDispatcher 객체를 상속받아 사용한다.
```kotlin
val singleThreadDispatcher: CoroutineDispatcher = newSingleThreadContext(name = "Parent")
launch(context = singleThreadDispatcher) {
    name() // 부모 코루틴
    launch { name() } // 자식 코루틴 실행
    launch { name() } // 자식 코루틴 실행
}
```
```kotlin
// 출력
[Parent @coroutine#2]
[Parent @coroutine#3]
[Parent @coroutine#4]
```

## 미리 정의된 CoroutineDispather
newFixedThreadPoolContext 함수를 사용하면 경고가 출력된다.  
```kotlin
This is a delicate API and its use requires care. 
Make sure you fully read and understand documentation of the declaration that is marked as a delicate API.
```
사용자가 지정한 스레드풀에 속한 스레드의 수가 너무 적거나 많이 생성돼 비효율적으로 동작할 수 있기 때문이다.  
그래서 코루틴 라이브러리는 미리 정의된 CoroutineDispatcher 객체인 IO, Default, Main을 제공한다.  
1. Dispatchers.IO : 입출력, 네트워크, DB 작업용. (JVM에서 사용이 가능한 프로세서의 수와 64 중 큰 값으로 스레드 개수를 설정)
2. Dispatchers.Default : CPU 바운드 작업, 대용량 데이터 처리
3. Dispatchers.Main : UI가 있는 애플리케이션에서 UI 업데이트 할 때 사용

## IO작업과 CPU 바운드 작업의 차이
= 작업이 실행됐을 때 스레드를 지속적으로 사용하는지의 여부  

일반적으로 IO 작업은 실행한 후 결과를 반환받을 때까지 스레드를 사용하지 않는다.  
반면 CPU 바운드 작업은 작업을 하는 동안 스레드를 지속적으로 사용한다.  

이 차이 떄문에 효율성에 차이가 생긴다.   

IO 작업을 코루틴을 사용해 실행하면 IO 작업 실행 후 스레드가 대기하는 동안 해당 스레드에 다른 IO 작업을 동시에 할 수 있어서 효율적이다.  
반면 CPU 바운드 작업은 코루틴을 사용하더라도 스레드가 지속적으로 사용되기 때문에 스레드 기반 작업을 사용해 실행했을 떄와 처리 속도에 큰 차이가 없다.

## IO와 Default는 코루틴 라이브러리에서 제공하는 공유 스레드풀을 사용한다.
```kotlin
repeat(2) {
    launch(Dispatchers.Default) { name() }
    launch(Dispatchers.IO) { name() }
}
```
이 코드 실행 결과 아래처럼 스레드 이름이 DefaultDispatcher-worker-n 이다.   
이는 둘이 같은 스레드풀을 사용한다는 것을 의미한다.
```kotlin
[DefaultDispatcher-worker-1 @coroutine#2]
[DefaultDispatcher-worker-1 @coroutine#3]
[DefaultDispatcher-worker-1 @coroutine#4]
[DefaultDispatcher-worker-2 @coroutine#5]
```

## limitedParallelism 함수를 사용해서 특정 연산을 위해 사용되는 스레드 수를 제한 할 수 있다.
#### 1. Dispatchers.Default
이 디스패처 객체에서 무겁고 오래 걸리는 연산을 처리하면 특정 연산을 위해 모든 스레드가 사용될 수 있다.    
이를 방지하기 위해 limitedParallelism 함수를 사용해서 스레드 개수를 제한할 수 있다.
```kotlin
launch(Dispatchers.Default.limitedParallelism(2)) { name() }
```
#### 2. Dispatchers.IO
Default의 limitedParallelism 함수와는 조금 다르다.  
공유 스레드풀의 스레드로 구성된 새로운 스레드풀을 만들어내며, 만들 수 있는 스레드 제한 없이 만들 수 있다.  
특정 작업이 다른 작업에 영향을 받지 않아야 해, 별도 스레드풀에서 실행되는 것이 필요할 때 사용돼야 한다.  
다만, 새로운 스레드를 만들어내는 것은 비싼 작업이므로 남용하지 말아야한다.  
```kotlin
launch(Dispatchers.IO.limitedParallelism(2)) { name() }
```
