# 에러 처리(초간단 3단계 + 최소 예시)

step1) 에러 발생 지점에서 try/catch 대신 예외를 직접 던진다(throw new ...)
step2) 전역 에러 핸들러(@RestControllerAdvice)가 예외를 잡아 공통 응답(ErrorResponse)으로 변환한다
step3) 해당 예외용 핸들러가 없으면, 기본(디폴트) 핸들러가 500으로 응답한다

예시 코드

// 1) 서비스: 그냥 예외를 던짐
@Service
class SampleService {
  void doWork(String input) {
    if (input == null || input.isBlank()) throw new IllegalArgumentException("입력 누락");
  }
}

// 2) 컨트롤러: try/catch 없음 → 예외는 전역 핸들러가 처리
@RestController
class SampleController {
  private final SampleService svc;
  SampleController(SampleService svc){ this.svc = svc; }

  @GetMapping("/sample")
  void sample(@RequestParam String input) { svc.doWork(input); }
}

// 3) 전역 핸들러: 표준 응답으로 변환(없으면 아래 Exception 핸들러가 디폴트)
@RestControllerAdvice
class GlobalExceptionHandler {
  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException ex, HttpServletRequest req) {
    return ResponseEntity.badRequest().body(new ErrorResponse(400, "Bad Request", ex.getMessage(), req.getRequestURI()));
  }
  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> fallback(Exception ex, HttpServletRequest req) {
    return ResponseEntity.status(500).body(new ErrorResponse(500, "Internal Server Error", "Unexpected error", req.getRequestURI()));
  }
}

