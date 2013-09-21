/**
 * <p>
 * 퀵 프로그래머에서 파일 처리에 관련된 부분을 모아놓은 패키지입니다.
 * </p><p>
 * 프로그램의 저장은 {@link net.wkbae.quickprogrammer.file.SaveManager}와 {@link net.wkbae.quickprogrammer.file.LoadManager}를 이용하여 이루어지며, <a href="http://json.org/">JSON</a>(<a href="http://code.google.com/p/json-simple/">json-simple</a>)을 이용합니다.
 * </p><p>
 * 플러그인을 불러올 때 {@link net.wkbae.quickprogrammer.file.PluginManager}를 이용합니다.<br>
 * 플러그인은 "plugin" 폴더에 jar 파일로 저장되며, jar 내부 루트에는 plugin.xml이 있어야 합니다.
 * <table border="1">
 * <tr><th>태그 이름</th><th>속성</th><th>설명</th><th>필수</th></tr>
 * <tr><th rowspan="6">Plugin</th><td colspan="2" style="text-align:center">항상 파일 최상단에 있어야 합니다. 그 외 태그는 Plugin 태그 내부에 있어야 하며, Plugin 태그는 하나만 있어야 합니다.</td><td>O</td></tr>
 * <tr><td>name</td><td>플러그인의 이름입니다. "블록 목록" 창에서 폴더 옆에 쓰여질 이름입니다.</td><td>O</td></tr>
 * <tr><td>identifier</td><td>플러그인의 식별자입니다. 다른 플러그인들과 구별하기 위한 것이며, "depends" 속성에 쓰입니다. 유저에게는 보여지지 않습니다.</td><td>O</td></tr>
 * <tr><td>version</td><td>플러그인의 버전입니다. 아직 쓰이지 않습니다. 나중에 업데이트 알림 기능이 추가될 예정입니다.</td><td>X</td></tr>
 * <tr><td>icon</td><td>플러그인의 아이콘입니다. "블록 목록" 창에서 플러그인의 이름 옆에 표시됩니다.</td><td>X</td></tr>
 * <tr><td>depends</td><td>이 플러그인이 의존(다른 플러그인이 있어야 이 플러그인이 실행)하는 플러그인의 식별자입니다. 여러 의존성을 가질 수 있으며, ","로 구분합니다. 플러그인은 의존성에 부합하는 경우에만 로드됩니다. 단, 상호 의존성, 순환 의존성은 허용되지 않으며, 그에 해당하는 플러그인들은 로드되지 않습니다.</td><td>X</td></tr>
 * <tr><th rowspan="4">Block</th><td colspan="2" style="text-align:center">코드 블록을 나타내는 태그입니다. 플러그인에서 이 태그를 이용해 블록을 추가합니다.</td><td>X</td></tr>
 * <tr><td>name</td><td>코드 블록의 이름입니다. "블록 목록" 창에서 플러그인 폴더 내부에 이 이름으로 표시됩니다.</td><td>O</td></tr>
 * <tr><td>class</td><td>코드 블록의 패키지+클래스입니다. 플러그인 파일에서 이 클래스를 찾아서 불러옵니다. 코드 블록은 반드시 {@link net.wkbae.quickprogrammer.BaseBlockModel} 클래스를 상속받아야 합니다.</td><td>O</td></tr>
 * <tr><td>icon</td><td>코드 블록의 아이콘입니다. "블록 목록" 창에서 블록의 이름 옆에 표시됩니다.</td><td>X</td></tr>
 * </table>
 * </p>
 */
package net.wkbae.quickprogrammer.file;