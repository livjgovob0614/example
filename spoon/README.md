
 테스트 코드
----------
 (1) LocalVarInfoTest

    이거부터 보심 돼요 설명 대강 써놨어요

 (2) MethodCallTest 

    MethodCallState.java, MethodInvocationSearch.java 얘네 써야하는데 import가 잘 안 돼서 그냥 직접 가져왔어요
    같은 패키지에 위치시키면 돼요

 (3) RemoveTryTest

    try - catch - final 지우고
    try body, final body만 남기는 예제


분석 방법
--------

 (1) 테스트 코드 만들어 적절한 디렉터리에 위치
 
   (제 기준) `.../spoon/examples/src/main/java/fr/inria/gforge/spoon/transformation/`

 (2) 테스트 코드에서 설정한 타겟 디렉터리에 분석하길 원하는 파일 올려두기

   `.../spoon/examples/src/test/resources/project/src/main/java/`

 (3) 컴파일
   `.../spoon/examples/` 에서 `mvn compile`


 (4) test cmd
   `.../spoon/examples/` 에서 `mvn test -Dtest=파일이름(.java 빼고)`

 (4) result :
   `.../spoon/examples/spooned/`
