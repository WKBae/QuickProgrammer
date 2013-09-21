Quick Programmer (퀵 프로그래머)
=============================

퀵 프로그래머는 누구나 쉽고 빠르게 프로그램을 제작할 수 있도록 도와주는 툴입니다.
화면상에 표시되는 "코드 블록"을 드래그하고 나열해서 프로그램을 제작하며, 아이들도 금방 배워 사용할 수 있을 정도로 간단합니다.
프로그래밍을 처음 시작할 때, 업무에서 간단한 프로그램 샘플을 제작할 때에도 좋습니다.

퀵 프로그래머는 [자바(Java)](http://www.java.com/)로 만들어져 자바가 지원되는 모든 운영체제에서 실행됩니다.

퀵 프로그래머를 사용하는 이유는?
-----------------------------
* 퀵 프로그래머의 목표는 "쉽고 빠르게"로, 직관적이며 풍부한 기능을 목표로 제작되었습니다.

인터넷 상에는 이것과 비슷한, 프로그램의 부분을 드래그해서 프로그램을 제작하는 툴이 이미 많이 있습니다.
하지만 그것들은 "프로그램"을 제작한다고 하기에도 어려울 정도로 단순하거나, 보통의 프로그래밍처럼 글로 쓰는 것보다도 복잡한 것들이 대다수입니다.

플러그인이란?
-----------------------------

위에서 언급한 것처럼 이미 많은 툴이 제작되었는데, 현재 관리가 되지 않는 툴도 많이 있습니다.
하지만 퀵 프로그래머는 프로그래밍을 할 줄 아는 유저들이 직접 기능을 추가하는 "플러그인"을 제작하여 공유하고 사용할 수 있습니다.
플러그인을 통해서 유저들은 수많은 기능들을 추가하여 활용할 수 있습니다.
플러그인은 퀵 프로그래머 파일과 같은 폴더에 있는 "plugin" 플더에 넣으면 됩니다.

플러그인 제작
-----------------------------

퀵 프로그래머의 플러그인은 [자바(Java)](http://www.java.com/)로 작성됩니다.
문서(Javadoc)를 참조하여 플러그인을 제작하고, plugin.xml 파일을 작성합니다.
plugin.xml의 형식은 XML을 따르며, 다음의 태그를 이용합니다.
<table border="1">
<tr><th>태그 이름</th><th>속성</th><th>설명</th><th>필수</th></tr>
<tr><th rowspan="6">Plugin</th><td colspan="3" style="text-align:center">항상 파일 최상단에 있어야 합니다. 그 외 태그는 Plugin 태그 내부에 있어야 하며, Plugin 태그는 하나만 있어야 합니다.</td></tr>
<tr><td>name</td><td>플러그인의 이름입니다. "블록 목록" 창에서 폴더 옆에 쓰여질 이름입니다.</td><td>O</td></tr>
<tr><td>identifier</td><td>플러그인의 식별자입니다. 다른 플러그인들과 구별하기 위한 것이며, "depends" 속성에 쓰입니다. 유저에게는 보여지지 않습니다.</td><td>O</td></tr>
<tr><td>version</td><td>플러그인의 버전입니다. 아직 쓰이지 않습니다. 나중에 업데이트 알림 기능이 추가될 예정입니다.</td><td>X</td></tr>
<tr><td>icon</td><td>플러그인의 아이콘입니다. "블록 목록" 창에서 플러그인의 이름 옆에 표시됩니다.</td><td>X</td></tr>
<tr><td>depends</td><td>이 플러그인이 의존(다른 플러그인이 있어야 이 플러그인이 실행)하는 플러그인의 식별자입니다. 여러 의존성을 가질 수 있으며, ","로 구분합니다. 플러그인은 의존성에 부합하는 경우에만 로드됩니다. 단, 상호 의존성, 순환 의존성은 허용되지 않으며, 그에 해당하는 플러그인들은 로드되지 않습니다.</td><td>X</td></tr>
<tr><th rowspan="4">Block</th><td colspan="3" style="text-align:center">코드 블록을 나타내는 태그입니다. 플러그인에서 이 태그를 이용해 블록을 추가합니다.</td></tr>
<tr><td>name</td><td>코드 블록의 이름입니다. "블록 목록" 창에서 플러그인 폴더 내부에 이 이름으로 표시됩니다.</td><td>O</td></tr>
<tr><td>class</td><td>코드 블록의 패키지+클래스입니다. 플러그인 파일에서 이 클래스를 찾아서 불러옵니다. 코드 블록은 반드시 net.wkbae.quickprogrammer.BaseBlockModel 클래스를 상속받아야 합니다.</td><td>O</td></tr>
<tr><td>icon</td><td>코드 블록의 아이콘입니다. "블록 목록" 창에서 블록의 이름 옆에 표시됩니다.</td><td>X</td></tr>
</table>

예시:
```xml
<Plugin name="Messages" identifier="net.wkbae.quickprogrammer.plugin.message">
	<Block name="Plain Message" class="net.wkbae.quickprogrammer.plugin.message.PlainMessageBlock" />
	<Block name="Information Message" class="net.wkbae.quickprogrammer.plugin.message.InformationMessageBlock" />
	<Block name="Question Message" class="net.wkbae.quickprogrammer.plugin.message.QuestionMessageBlock" />
	<Block name="Warning Message" class="net.wkbae.quickprogrammer.plugin.message.WarningMessageBlock" />
	<Block name="Error Message" class="net.wkbae.quickprogrammer.plugin.message.ErrorMessageBlock" />
</Plugin>
```
