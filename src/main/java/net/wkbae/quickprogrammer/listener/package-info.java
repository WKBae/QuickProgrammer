/**
 * 퀵 프로그래머에서 사용되는 각종 리스너들을 모아둔 패키지입니다.<br>
 * 내부적으로 많이 쓰이지만, 플러그인에서 직접 사용할 수도 있습니다. 성능 저하, 메모리 누수를 방지하기 위해서 사용되지 않을 때에는({@link net.wkbae.quickprogrammer.BaseBlockModel#onBlockRemove()} 호출시 등)반드시 리스너를 해제하는 것이 좋습니다. 
 */
package net.wkbae.quickprogrammer.listener;