package com.dgraphtech.controller;

import com.dgraphtech.entities.Event;
import com.dgraphtech.entities.Subscriber;
import com.dgraphtech.models.Notification;
import com.dgraphtech.repositories.EventRepository;
import com.dgraphtech.repositories.SubscriberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
public class WebhookController {

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * This enpoint is used when subscriber subcribes with the webhook provider.
     * @param subscriber
     * @return
     */
    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestBody Subscriber subscriber) {
        Optional<Subscriber> existingSubscriber = subscriberRepository.findByCallbackUrl(subscriber.getCallbackUrl());
        if (existingSubscriber.isPresent()) {
            return ResponseEntity.badRequest().body("Subscriber already exists with the same callback URL");
        }
        subscriberRepository.save(subscriber);
        return ResponseEntity.ok("Subscribed successfully");
    }

    /**
     * This endpoint is used by webhook provider. When an event occurs, the event listener at the webhook provider will trigger this API
     * to publish/notify the event to all the subscriber's subscribed to that event. Some times we don't need this endpoint, we can directly notify the
     * event from the event listeners. For example, if we got event on to a queue, the queue listener can notify the event to susbscribers (Push)
     * @param event
     * @return
     */
    @PostMapping("/publish")
    public ResponseEntity<String> publishEvent(@RequestBody Event event) {
        // Save the event to the database and notify the subscriber
        eventRepository.save(event);
        notifySubscriber(event);
        return ResponseEntity.ok("Event published successfully");
    }

    private void notifySubscriber(Event event) {
        sendHttpRequest(event);
    }

    private void sendHttpRequest(Event event) {
        //As we are building a simple notification application, we are getting all the susbscribers to notify.
        // In a robust implementation we will get only the subscribers subscribed for a specific event.
        List<Subscriber> subscribers = subscriberRepository.findAll();
        for (Subscriber subscriber : subscribers) {
            HttpEntity<Notification> request = new HttpEntity<>(new Notification(event.getId(), event.getEventType(), event.getEventData()));
            ResponseEntity<Notification> response = restTemplate.exchange(subscriber.getCallbackUrl(), HttpMethod.POST, request, Notification.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("The notification is sent successfully!! to Subscriber :::" + subscriber.getCallbackUrl());
            }
        }
    }
}

