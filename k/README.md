# 코루틴 심화

### 학습 목표

1. 코루틴이 공유 상태를 사용할 때의 문제와 다양한 데이터 동기화 방식들
2. 코루틴에 다양한 실행 옵션 부여하기
3. 무제한 디스패처의 동작 방식
4. 코루틴에서 일시 중단과 재개가 일어나는 원리

## JVM 메모리 공간과 하드웨어 메모리 공간의 연결

- JVM은 아래 그림과 같이 스레드마다 스택영역을 갖고있고, 힙 영역은 공통으로 갖고있다.
- 하드웨어의 메모리 구조를 보면 CPU 레지스터, CPU 캐시 메모리, 메인 메모리 영역으로 구성된다. 각 CPU는 CPU 캐시 메로리를 두며, 데이터 조회 시 공통 영역인 메인 메모리까지 가지 않고 CPU 캐시
  메모리에서 데이터를 조회할 수 있도록 만들어 속도를 향상 시킨다.

todo : image

하드웨어 메모리 구조는 JVM의 스택 영역과 힙 영역을 구분하지 않는다. 따라서 JVM 스택 영역에 저장된 데이터들은 CPU 레지스터, CPU 캐시 메모리, 메인 메모리 모두에 나타날 수 있으며, 힙 영역도
마찬가지다.

이런 구조로 인해 멀티 스레드 환경에 공유 상태를 사용할 때 두가지 문제가 발생한다.

1. 공유 상태에 대한 가시성 문제
2. 공유 상태에 대한 경쟁 상태 문제

### 1. 공유 상태에 대한 가시성 문제

하나의 스레드가 다른 스레드가 변경된 상태를 확인하지 못하는 것이다.

공유 상태는 처음에는 메인 메모리상에 저장돼 있다. 이때 하나의 스레드가 공유 상태를 읽어 오면 해당 스레드를 실행 중인 CPU는 공유 상태를 CPU 캐시 메모리에 저장한다.    
연산이 완료되면 스레드는 이 정보를 메인 메모리에 쓰지 않고 CPU 캐시 메모리에 쓴다. 변경된 값은 플러시가 일어나지 않으면 메인 메모리로 전파되지 않는다.   
만약 전파되지 않은 상태에서 다른 CPU의 스레드에서 메인 메모리 값을 읽는다면 변경되지 않는 값을 읽게 된다.  
이런 메모리 동기화 문제를 메모리 가시성 문제라고 한다.

- 해결하기
  Vilatile 어노테이션을 사용하면 CPU 캐시메모리를 사용하지 않고 메인 메모리를 사용한다. (스레드-메인 메모리 연결)

```kotlin
@Volatile
var count = 0
```

### 2. 공유 상태에 대한 경쟁 상태 문제

하지만 여전히 스레드가 동시에 접근할 수 있기 때문에 문제가 발생한다.  
이런 경쟁 상태를 해결하기 위해 스레드가 동시에 접근할 수 없도록 만들어야 한다.

#### 2-1. Mutex 사용해 동시 접근 제한하기

공유 변수의 변경 가능 지점을 임계 영역으로 만들어 동시 접근을 제한하는 방식.  
코루틴의 Mutex 객체에 lock 함수가 호출되면 락이 획득되며, 이후 unlock이 호출돼 락이 해제될 때까지 다른 코루틴이 임계 영역에 진입할 수 없다.  
lock-unlock 쌍은 실수를 유발할 수 있으므로 withLock 함수를 사용하는 것이 좋다.

##### Mutex vs ReetranLock

- Mutex : 이미 다른 코루틴에 의해 Mutex 객체에 락이 걸려있으면 스레드 양보
- ReetranLock : 호출 스레드를 블로킹하고 기다림

#### 2-2. 전용 스레드 만들기

복수의 스레드가 공유 상태에 동시에 접근할 수 있기에 일어나는 문제기 때문에, 하나의 전용 스레드를 만들어서 문제를 해결할 수 있다.  
`newSingleThreadContext` 함수 사용

##### 원자성 있는 데이터 구조

경쟁 상태 해결을 위해 원자성 있는 객체를 사용할 수 있다.  
AtomicInteger, AtomicLong, AtomicBoolean 으로 간단한 타입에 원자성을 부여할 수 있고,  
AtomicReference를 사용하면 복잡한 객체에도 원자성을 부여할 수 있다.

다만, 원자성 있는 객체에 접근할 떄 이미 다른 스레드의 코루틴이 해당 객체에 대한 연산을 실행 중인 경우 코루틴은 스레드를 블로킹하고 기다린다.  
마치 ReetrantLock 객체와 비슷하다.

그리고 읽기와 쓰기를 따로 하면 그 사이에 다른 스레드가 접근이 가능하므로 읽기와 쓰기를 동시에하는 함수를 사용해야한다.

## CoroutineStart의 다양한 옵션

```kotlin
public fun CoroutineScope.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job
```

#### 1. DEFAULT

기본값으로 DEFAULT가 설정된다. 생성된 코루틴의 실행을 CoroutineDispatcher 객체에 예약한다.

#### 2. ATOMIC

코루틴이 실행 대기 상태일 때 취소를 요청해도 해당 코루틴은 취소되지 않는다.

```kotlin
val job = launch(start = CoroutineStart.ATOMIC) {
    println("1")
}
job.cancel()  // 실행대기 상태인 job에 취소 명령을 내려도 취소 되지 않음
println("2")

// 실행 결과
2
1
```

#### 3. LAZY

코루틴 실행을 미룬다. start 함수를 호출해야지만 실행한다.

#### 4. UNDISPATCHED

CoroutineDispatcher 객체의 작업 대기열을 거치지 않고 호출자의 스레드에서 즉시 실행한다.      
만약 코루틴 내부에서 일시 중단 후 재개될 때는 CoroutineDispatcher 개체를 거쳐 실행된다.

## 무제한 디스패처

무제한 디스패처란? 자신을 실행시킨 스레드에서 즉시 실행하도록 만드는 디스패처. 이 때 호출된 스레드가 무엇이든 상관 없기 때문에 실행 스레드가 제한되지 않음.

```kotlin
fun main() = runBlocking {
    launch(Dispatchers.Unconfined) {
        // Main 스레드에서 즉시 실행
    }
}
```

하지만 일시 중단 후 재개될 때는 재개시키는 스레드에서 실행된다.

```kotlin
runBlocking(Dispatchers.Unconfined) {
    name()
    val job = launch {
        delay(10)
        name()
    }
}
```

```kotlin
// 결과
[main @coroutine #2]
[kotlinx.coroutines.DefaultExecutor @coroutine #3] // delay 함수를 실행하는 스레드
```

