package com.sensorberg.front.resolve.resources.layout

import com.sensorberg.front.resolve.resources.layout.domain.LayoutCtx
import com.sensorberg.front.resolve.resources.layout.domain.LayoutRequest
import com.sensorberg.front.resolve.resources.layout.domain.LayoutRequestAction
import com.sensorberg.front.resolve.resources.layout.domain.LayoutRequestBody
import com.sensorberg.front.resolve.resources.layout.domain.LayoutRequestEvent
import com.sensorberg.front.resolve.service.AzureEventHubService
import spock.lang.Specification

/**
 * Created by Andreas Dörner on 25.08.16.
 */
/**
 * Created by Andreas Dörner on 02.05.16.
 */
class LayoutServiceSplitTest extends Specification {

    def "split null context"() {
        given:
        def tested = new LayoutService()
        def azureEventHubService = Mock(AzureEventHubService)

        when: "split data in layout context"
        tested.splitLayoutCtxAndWriteToAzure(null)

        then: "no invocation"
        0 * azureEventHubService.checkObjectSize()
        0 * azureEventHubService.sendAsyncObjectMessage()
    }

    def "split context both 3500 entries"() {
        given:
        def tested = new LayoutService()
        def azureEventHubService = Mock(AzureEventHubService)

        // inject
        tested.azureEventHubService = azureEventHubService

        def layoutCtx = new LayoutCtx()
        layoutCtx.setRequest(new LayoutRequest())
        layoutCtx.getRequest().setActivity(new LayoutRequestBody())
        layoutCtx.getRequest().getActivity().setActions(new ArrayList<LayoutRequestAction>())
        layoutCtx.getRequest().getActivity().setEvents(new ArrayList<LayoutRequestEvent>())

        for (int i = 1; i < 3500; i++) {
            def layoutRequestEvent = new LayoutRequestEvent()
            layoutRequestEvent.setLocation("dummy" + i)
            layoutRequestEvent.setPid("dummy" + i)
            layoutRequestEvent.setTrigger(1)

            layoutCtx.getRequest().getActivity().getEvents().add(layoutRequestEvent)
            layoutCtx.getRequest().getActivity().getActions().add(createAction(i))
        }

        when: "split data in layout context"
        tested.splitLayoutCtxAndWriteToAzure(layoutCtx)

        then: "count invocation"
        5 * azureEventHubService.sendSynchronousObjectMessage(_)
    }

    def "split context only events 3500 entries"() {
        given:
        def tested = new LayoutService()
        def azureEventHubService = Mock(AzureEventHubService)

        // inject
        tested.azureEventHubService = azureEventHubService

        def layoutCtx = new LayoutCtx()
        layoutCtx.setRequest(new LayoutRequest())
        layoutCtx.getRequest().setActivity(new LayoutRequestBody())
        layoutCtx.getRequest().getActivity().setActions(new ArrayList<LayoutRequestAction>())
        layoutCtx.getRequest().getActivity().setEvents(new ArrayList<LayoutRequestEvent>())

        for (int i = 1; i < 3500; i++) {
            LayoutRequestEvent layoutRequestEvent = new LayoutRequestEvent();
            layoutRequestEvent.setLocation("dummy" + i)
            layoutRequestEvent.setPid("dummy" + i)

            layoutCtx.getRequest().getActivity().getEvents().add(layoutRequestEvent)
        }

        when: "split data in layout context"
        tested.splitLayoutCtxAndWriteToAzure(layoutCtx)

        then: "count invocation"
        5 * azureEventHubService.sendSynchronousObjectMessage(_)
    }

    def "split context events 2000 actions 1000 entries"() {
        given:
        def tested = new LayoutService()
        def azureEventHubService = Mock(AzureEventHubService)

        // inject
        tested.azureEventHubService = azureEventHubService

        def layoutCtx = new LayoutCtx()
        layoutCtx.setRequest(new LayoutRequest())
        layoutCtx.getRequest().setActivity(new LayoutRequestBody())
        layoutCtx.getRequest().getActivity().setActions(new ArrayList<LayoutRequestAction>())
        layoutCtx.getRequest().getActivity().setEvents(new ArrayList<LayoutRequestEvent>())

        for (int i = 1; i < 2001; i++) {
            def layoutRequestEvent = new LayoutRequestEvent();
            layoutRequestEvent.setLocation("dummy" + i)
            layoutRequestEvent.setPid("dummy" + i)

            layoutCtx.getRequest().getActivity().getEvents().add(layoutRequestEvent)
        }

        for (int i = 1; i < 1001; i++) {
            layoutCtx.getRequest().getActivity().getActions().add(createAction(i))
        }

        when: "split data in layout context"

        tested.splitLayoutCtxAndWriteToAzure(layoutCtx)

        then: "count invocation"
        3 * azureEventHubService.sendSynchronousObjectMessage(_)
    }

    def "split context events 1000 actions 2000 entries"() {
        given:
        def tested = new LayoutService()
        def azureEventHubService = Mock(AzureEventHubService)

        // inject
        tested.azureEventHubService = azureEventHubService

        def layoutCtx = new LayoutCtx()
        layoutCtx.setRequest(new LayoutRequest())
        layoutCtx.getRequest().setActivity(new LayoutRequestBody())
        layoutCtx.getRequest().getActivity().setActions(new ArrayList<LayoutRequestAction>())
        layoutCtx.getRequest().getActivity().setEvents(new ArrayList<LayoutRequestEvent>())

        for (int i = 1; i < 1001; i++) {
            def layoutRequestEvent = new LayoutRequestEvent();
            layoutRequestEvent.setLocation("dummy" + i)
            layoutRequestEvent.setPid("dummy" + i)

            layoutCtx.getRequest().getActivity().getEvents().add(layoutRequestEvent)
        }

        for (int i = 1; i < 2001; i++) {
            layoutCtx.getRequest().getActivity().getActions().add(createAction(i))
        }

        when: "split data in layout context"

        tested.splitLayoutCtxAndWriteToAzure(layoutCtx)

        then: "count invocation"
        3 * azureEventHubService.sendSynchronousObjectMessage(_)
    }


    def "split context events 740 actions 760 entries"() {
        given:
        def tested = new LayoutService()
        def azureEventHubService = Mock(AzureEventHubService)

        // inject
        tested.azureEventHubService = azureEventHubService

        def layoutCtx = new LayoutCtx()
        layoutCtx.setRequest(new LayoutRequest())
        layoutCtx.getRequest().setActivity(new LayoutRequestBody())
        layoutCtx.getRequest().getActivity().setActions(new ArrayList<LayoutRequestAction>())
        layoutCtx.getRequest().getActivity().setEvents(new ArrayList<LayoutRequestEvent>())

        for (int i = 1; i < 741; i++) {
            def layoutRequestEvent = new LayoutRequestEvent();
            layoutRequestEvent.setLocation("dummy" + i)
            layoutRequestEvent.setPid("dummy" + i)

            layoutCtx.getRequest().getActivity().getEvents().add(layoutRequestEvent)
        }

        for (int i = 1; i < 761; i++) {
            layoutCtx.getRequest().getActivity().getActions().add(createAction(i))
        }

        when: "split data in layout context"

        tested.splitLayoutCtxAndWriteToAzure(layoutCtx)

        then: "count invocation"
        2 * azureEventHubService.sendSynchronousObjectMessage(_)
    }

    def LayoutRequestAction createAction(int i) {
        def layoutRequestAction = new LayoutRequestAction()

        layoutRequestAction.eid = "dummy" + i
        layoutRequestAction.trigger = 1

        def map = new HashMap<String, Object>()
        map.put("payload", "www.nase.de")
        map.put("url", "www.nase.de")
        map.put("text", "www.nase.de")

        return layoutRequestAction
    }
}
