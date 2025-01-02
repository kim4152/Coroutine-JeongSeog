# 코루틴 빌더와 Job

모든 코루틴 빌더 함수는 Job 객체를 생성한다.  
이 Job 객체를 통해 코루틴을 제어할 수 있다.

---

#### join
Job 객체가 완료될 때까지 코루틴을 일시 중단한다.   
이미 실행 중인 다른 코루틴을 일시 중단하지는 않는다.

#### joinAll
다음과 같이 가변인자로 Job 객체를 받고, 각 객체에 대해 모두 join 함수를 호출한다.
```kotlin
public suspend fun joinAll(vararg jobs: Job): Unit = jobs.forEach { it.join() }
```

#### CoroutineStart.LAZY
코루틴을 생성하면 바로 실행상태가 되지만 지연 시작할 수 있는 기능을 제공한다.
```kotlin
val job = launch(
        start = CoroutineStart.LAZY
    ) { ... }
```

명시적으로 실행을 요청하지 않으면 시작되지 않는다.
```kotlin
job.start() // 명시적으로 요청
```

#### cancel
Job 객체를 사용해 코루틴을 취소한다. 

#### cancelAndJoin
코루틴의 cancel이 호출된다고 바로 취소되는 것이 아니다.  
cancel 이나 cancelAndJoin이 호출되면 Job 객체 내부에 있는 취소 확인용 플래그를 바꾸기만 하며,   
코루틴이 이 플래그를 확인하는 시점에 비로소 취소된다.
따라서 순차처리가 중요하다면 cancelAndJoin 함수를 사용해야한다.
```kotlin
val job = launch{}
job.cancelAndJoin() // job이 취소될 때까지 코루틴을 일시 중단한다.
afterJobCancelled() // job이 취소되고 실행
```

#### 코루틴의 취소 확인
코루틴의 취소 플래그 확인 시점은 일반적으로 일시 중단 지점이나 코루틴이 실행을 대기하는 시점이다.
1. delay를 사용한 취소 확인
2. yield를 사용한 취소 확인
3. CoroutineScope.isActive를 사용한 취소 확인



## 코루틴의 상태와 Job의 상태 변수
- 코루틴의 상태
1. 생성 : 코루틴을 생성하면 기본적으로 생성 상태에 놓이며 자동으로 실행 중 상태로 넘어간다. Lazy 옵션을 사용하면 실행 중 상태로 넘어가지 않는다.
2. 실행 중 : 자동으로 실행 중 상태로 넘어온다. 일시 중단된 때에도 실행 중 상태로 본다.
3. 실행 완료 : 코루틴의 모든 코드가 실행완료된 경우
4. 취소 중 : 코루틴에 취소가 요청됐을 경우. 아직 취소가 된 상태가 아니어서 코루틴은 계속 실행된다.
5. 취소 완료 : 취소 확인 시점에 취소가 확인된 경우
6. (실행 완료 중 -> 나중에)

- 상태 변수
1. isActive : 활성화 여부 . 취소가 요청되거나 실행이 완료된 코루틴은 활성화되지 않은 것으로 본다.
2. isCancelled : 취소 요청됐는지 여부. 요청되기만 하면 true 반환. true 더라도 즉시 취소되는 것은 아님
3. isCompleted : 코루틴 실행이 완료됐는지 여부

---

| 코루틴 상태 | isActive | isCancelled | isCompleted |
| --- | --- | --- | ---|
| 생성 | false | false | false |
| 실행 중 | true | false | false |
| 실행 완료 | false | false | true |
| 취소 중 | false | true | false |
| 취소 완료 | false | true | true |

