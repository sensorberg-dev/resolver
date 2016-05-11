package com.sensorberg.front.resolve.resources.layout

import com.sensorberg.front.resolve.resources.layout.domain.LayoutCtx
import com.sensorberg.front.resolve.resources.layout.domain.LayoutRequest
import com.sensorberg.front.resolve.resources.layout.domain.LayoutRequestAction
import com.sensorberg.front.resolve.resources.layout.domain.LayoutRequestBody
import com.sensorberg.front.resolve.resources.layout.domain.LayoutRequestEvent
import com.sensorberg.front.resolve.service.AzureEventHubService
import spock.lang.Specification
/**
 * Created by Andreas Dörner on 02.05.16.
 */
class LayoutServiceTest extends Specification {

    def "split null context"() {
        given:
        def tested = new LayoutService()
        def azureEventHubService = Mock(AzureEventHubService)

        when: "split data in layout context"
        tested.splitLayoutCtxAndWriteToAzure(null)

        then: "no invocation"
        0 * azureEventHubService.checkObjectSize()
        0 * azureEventHubService.sendObjectMessage()
    }

    def "split context both 3500 entries"() {
        given:
        def tested = new LayoutService()
        def azureEventHubService = Mock(AzureEventHubService)

        // inject
        // TODO: is there a better way ?
        tested.azureEventHubService = azureEventHubService

        def layoutCtx = new LayoutCtx()
        layoutCtx.setRequest(new LayoutRequest())
        layoutCtx.getRequest().setActivity(new LayoutRequestBody())
        layoutCtx.getRequest().getActivity().setActions(new ArrayList<LayoutRequestAction>())
        layoutCtx.getRequest().getActivity().setEvents(new ArrayList<LayoutRequestEvent>())

        for (int i = 1; i < 3500; i++) {
            LayoutRequestEvent layoutRequestEvent = new LayoutRequestEvent();
            layoutRequestEvent.setBid("dummy" + i)
            layoutRequestEvent.setLocation("dummy" + i)
            layoutRequestEvent.setPid("dummy" + i)

            layoutCtx.getRequest().getActivity().getEvents().add(layoutRequestEvent)

            LayoutRequestAction layoutRequestAction = new LayoutRequestAction();
            layoutRequestAction.setEid("dummy" + i)
            layoutRequestAction.setLocation("dummy" + i)
            layoutRequestAction.setPid("dummy" + i)

            layoutCtx.getRequest().getActivity().getActions().add(layoutRequestAction)
        }

        when: "split data in layout context"
        tested.splitLayoutCtxAndWriteToAzure(layoutCtx)

        then: "count invocation"
        7 * azureEventHubService.sendObjectMessage(_)
    }

    def "split context only events 3500 entries"() {
        given:
        def tested = new LayoutService()
        def azureEventHubService = Mock(AzureEventHubService)

        // inject
        // TODO: is there a better way ?
        tested.azureEventHubService = azureEventHubService

        def layoutCtx = new LayoutCtx()
        layoutCtx.setRequest(new LayoutRequest())
        layoutCtx.getRequest().setActivity(new LayoutRequestBody())
        layoutCtx.getRequest().getActivity().setActions(new ArrayList<LayoutRequestAction>())
        layoutCtx.getRequest().getActivity().setEvents(new ArrayList<LayoutRequestEvent>())

        for (int i = 1; i < 3500; i++) {
            LayoutRequestEvent layoutRequestEvent = new LayoutRequestEvent();
            layoutRequestEvent.setBid("dummy" + i)
            layoutRequestEvent.setLocation("dummy" + i)
            layoutRequestEvent.setPid("dummy" + i)

            layoutCtx.getRequest().getActivity().getEvents().add(layoutRequestEvent)
        }

        when: "split data in layout context"
        tested.splitLayoutCtxAndWriteToAzure(layoutCtx)

        then: "count invocation"
        7 * azureEventHubService.sendObjectMessage(_)
    }

    def "split context events 455 actions 4100 entries"() {
        given:
        def tested = new LayoutService()
        def azureEventHubService = Mock(AzureEventHubService)

        // inject
        // TODO: is there a better way ?
        tested.azureEventHubService = azureEventHubService

        def layoutCtx = new LayoutCtx()
        layoutCtx.setRequest(new LayoutRequest())
        layoutCtx.getRequest().setActivity(new LayoutRequestBody())
        layoutCtx.getRequest().getActivity().setActions(new ArrayList<LayoutRequestAction>())
        layoutCtx.getRequest().getActivity().setEvents(new ArrayList<LayoutRequestEvent>())

        for (int i = 1; i < 456; i++) {
            LayoutRequestEvent layoutRequestEvent = new LayoutRequestEvent();
            layoutRequestEvent.setBid("dummy" + i)
            layoutRequestEvent.setLocation("dummy" + i)
            layoutRequestEvent.setPid("dummy" + i)

            layoutCtx.getRequest().getActivity().getEvents().add(layoutRequestEvent)
        }

        for (int i = 1; i < 4101; i++) {
            LayoutRequestAction layoutRequestAction = new LayoutRequestAction();
            layoutRequestAction.setEid("dummy" + i)
            layoutRequestAction.setLocation("dummy" + i)
            layoutRequestAction.setPid("dummy" + i)

            layoutCtx.getRequest().getActivity().getActions().add(layoutRequestAction)
        }

        when: "split data in layout context"
        tested.splitLayoutCtxAndWriteToAzure(layoutCtx)

        then: "count invocation"
        9 * azureEventHubService.sendObjectMessage(_)
    }
}
