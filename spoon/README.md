 (1) LocalVarInfoTest
    이거부터 보심 돼요 설명 대강 써놨어요

 (2) MethodCallTest 
    MethodCallState.java, MethodInvocationSearch.java 얘네 써야하는데 import가 잘 안 돼서 그냥 직접 가져왔어요
    같은 패키지에 위치시키면 돼요

 (3) RemoveTryTest
    try - catch - final 지우고
    try body, final body만 남기는 예제


 * resource :
    .../spoon/examples/src/test/resources/project/src/main/java/

 * result :
    .../spoon/examples/spooned/

 * compile :
    .../spoon/examples/ 에서 mvn compile

 * test :
    .../spoon/examples/ 에서 mvn test -Dtest=파일이름(.java 빼고)

